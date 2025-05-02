package org.shirakawatyu.btmetadataserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.shirakawatyu.btmetadataserver.bittorrent.MetaInfoApi;
import org.shirakawatyu.btmetadataserver.bittorrent.TrackerApi;
import org.shirakawatyu.btmetadataserver.exception.MetadataNotFoundException;
import org.shirakawatyu.btmetadataserver.mapper.ExtraDataMapper;
import org.shirakawatyu.btmetadataserver.mapper.MetadataMapper;
import org.shirakawatyu.btmetadataserver.pojo.*;
import org.shirakawatyu.btmetadataserver.service.MetadataService;
import org.shirakawatyu.btmetadataserver.util.RequestUtil;
import org.shirakawatyu.btmetadataserver.util.StringUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MetadataServiceImpl implements MetadataService {
    private final MetaInfoApi metaInfoApi;
    private final TrackerApi trackerApi;
    private final String peerId;
    private final MetadataMapper metadataMapper;
    private final ExtraDataMapper extraDataMapper;
    private final String rssUrl;
    private final int rssCurr;
    private final ConcurrentHashMap<String, FutureTask<Metadata>> queryTasks = new ConcurrentHashMap<>();

    public MetadataServiceImpl(MetaInfoApi metaInfoApi, TrackerApi trackerApi, MetadataMapper metadataMapper, ExtraDataMapper extraDataMapper, BittorrentConfig config) {
        this.metaInfoApi = metaInfoApi;
        this.trackerApi = trackerApi;
        this.metadataMapper = metadataMapper;
        this.extraDataMapper = extraDataMapper;
        this.peerId = "-MS3939-" + StringUtil.random(12);
        this.rssUrl = config.getRss().getUrl();
        this.rssCurr = config.getRss().getConcurrency();
    }

    @Override
    public Map<String, Object> getMetadata(String infoHash) {
        Map<String, Object> info = selectMetadataById(infoHash).getInfo();
        ExtraData extraData = extraDataMapper.selectById(infoHash);
        if (extraData == null) {
            extraData = new ExtraData(infoHash, "unknown", "unknown", "unknown");
        }
        info.put("extra", extraData);
        return info;
    }

    @Override
    public byte[] getTorrent(String infoHash) {
        return selectMetadataById(infoHash).getTorrent();
    }

    private Metadata selectMetadataById(String infoHash) {
        Metadata metadata = metadataMapper.selectById(infoHash);
        if (metadata == null) {
            if (!queryTasks.containsKey(infoHash)) {
                try {
                    FutureTask<Metadata> task = new FutureTask<>(() -> {
                        List<Peer> peers = trackerApi.requestTracker(new TrackerReq(infoHash, peerId));
                        Metadata m = metaInfoApi.getMetaInfo(infoHash, peers, peerId);
                        if (m != null) {
                            metadataMapper.insert(m);
                        }
                        return m;
                    });
                    queryTasks.put(infoHash, task);
                    Thread.startVirtualThread(task);
                } catch (Exception e) {
                    throw new MetadataNotFoundException();
                }
            }
            FutureTask<Metadata> task = queryTasks.get(infoHash);
            queryTasks.remove(infoHash);
            try {
                metadata = task.get();
            } catch (InterruptedException | ExecutionException ignored) {
                throw new MetadataNotFoundException();
            }
        }
        if (metadata == null) {
            throw new MetadataNotFoundException();
        }
        return metadata;
    }

    @Scheduled(cron = "${bittorrent.rss.cron}")
    public void updateMetadata() {
        String xml = RequestUtil.getForString(rssUrl);
        if (xml == null) {
            return;
        }
        Document doc = Jsoup.parse(xml, Parser.xmlParser());
        Elements items = ((Element) doc.childNodes().get(1)).getElementsByTag("item");
        Pattern pattern = Pattern.compile("urn:btih:([a-zA-Z0-9]+)");
        ArrayList<FutureTask<Boolean>> tasks = new ArrayList<>();
        AtomicInteger con = new AtomicInteger(rssCurr);
        for (Element item : items) {
            String magnet = item.getElementsByTag("enclosure").attr("url");
            String publish = item.getElementsByTag("pubDate").text();
            String category = item.getElementsByTag("category").text();
            String title = item.getElementsByTag("title").text();
            Matcher matcher = pattern.matcher(magnet);
            if (matcher.find()) {
                String infoHash = matcher.group(1);
                if (metadataMapper.selectById(infoHash) != null) {
                    continue;
                }
                FutureTask<Boolean> task = new FutureTask<>(() -> {
                    try {
                        selectMetadataById(infoHash);
                        extraDataMapper.insertOrUpdate(new ExtraData(infoHash, title, publish, category));
                        log.info("Successfully got metadata of {} from RSS feed", title);
                        return true;
                    } catch (MetadataNotFoundException e) {
                        log.warn("Failed to get metadata of {} from RSS feed, {}", title, infoHash);
                        return false;
                    } finally {
                        con.incrementAndGet();
                    }
                });
                while (con.get() <= 0) {
                    Thread.onSpinWait();
                }
                Thread.startVirtualThread(task);
                tasks.add(task);
                con.decrementAndGet();
            }
        }
        int success = 0;
        for (FutureTask<Boolean> task : tasks) {
            try {
                if (task.get()) {
                    success++;
                }
            } catch (Exception ignored) {}
        }
        log.info("Updated {} metadata from RSS feed", success);
    }
}
