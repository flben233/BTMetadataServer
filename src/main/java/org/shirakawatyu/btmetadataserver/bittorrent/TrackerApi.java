package org.shirakawatyu.btmetadataserver.bittorrent;

import lombok.extern.slf4j.Slf4j;
import org.shirakawatyu.btmetadataserver.pojo.BittorrentConfig;
import org.shirakawatyu.btmetadataserver.pojo.Peer;
import org.shirakawatyu.btmetadataserver.pojo.TrackerReq;
import org.shirakawatyu.btmetadataserver.util.RequestUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicIntegerArray;


@Slf4j
@Component
public class TrackerApi {
    private final List<String> trackerList;
    private final AtomicIntegerArray trackerStatus;

    public TrackerApi(BittorrentConfig config) {
        this.trackerList = config.getTrackerList();
        this.trackerStatus = new AtomicIntegerArray(this.trackerList.size());
    }

    public List<Peer> requestTracker(TrackerReq trackerBody) {
        List<FutureTask<List<Peer>>> tasks = new ArrayList<>();
        for (int i = 0; i < trackerList.size(); i++) {
            String tracker = trackerList.get(i);
            int finalI = i;
            FutureTask<List<Peer>> task = new FutureTask<>(() -> {
                if (trackerStatus.get(finalI) == -1) {
                    return new ArrayList<>(0);
                }
                try {
                    List<Peer> peers = requestTrackerSingle(tracker, trackerBody);
                    if (peers.isEmpty()) {
                        trackerStatus.set(finalI, -1);
                    }
                    return peers;
                } catch (Exception e) {
                    log.warn("Error requesting tracker: {}", tracker);
                    trackerStatus.set(finalI, -1);
                    return new ArrayList<>(0);
                }
            });
            Thread.startVirtualThread(task);
            tasks.add(task);
        }
        Set<Peer> result = new HashSet<>();
        for (FutureTask<List<Peer>> task : tasks) {
            try {
                List<Peer> peers = task.get();
                result.addAll(peers);
            } catch (Exception e) {
                log.error("Error requesting tracker", e);
            }
        }
        return result.stream().toList();
    }

    public List<Peer> requestTrackerSingle(String trackerUrl, TrackerReq trackerBody) {
        trackerUrl = trackerUrl + trackerBody.toQueryString() + "&compact=1&no_peer_id=1&numwant=200";
        byte[] bytes = RequestUtil.getForBytes(trackerUrl);
        if (bytes == null) {
            return new ArrayList<>(0);
        }
        String s = new String(bytes, StandardCharsets.UTF_8);
        int lenStart = s.indexOf("peers") + 5;
        int headerEnd = s.indexOf(":", lenStart);
        int peersLen = Integer.parseInt(s.substring(lenStart, headerEnd));
        return bytesToPeers(Arrays.copyOfRange(bytes, headerEnd + 1, headerEnd + 1 + peersLen));
    }

    public List<Peer> bytesToPeers(byte[] bytes) {
        List<Peer> hosts = new ArrayList<>();
        if (bytes.length % 6 != 0) {
            log.error("Invalid peer list length: {}", bytes.length);
            return hosts;
        }
        for (int i = 0; i < bytes.length; i += 6) {
            String ip = String.format("%d.%d.%d.%d", bytes[i] & 0xFF, bytes[i + 1] & 0XFF, bytes[i + 2] & 0XFF, bytes[i + 3] & 0XFF);
            String port = String.valueOf(((bytes[i + 4] & 0xFF) << 8) | (bytes[i + 5] & 0xFF));
            hosts.add(new Peer(ip, Integer.parseInt(port)));
        }
        return hosts;
    }

    @Scheduled(cron = "${bittorrent.check-trackers-cron}")
    public void checkTrackers() {
        for (int i = 0; i < trackerList.size(); i++) {
            String tracker = trackerList.get(i);
            int finalI = i;
            Thread.startVirtualThread(() -> {
                try {
                    byte[] bytes = RequestUtil.getForBytes(tracker);
                    if (bytes == null) {
                        trackerStatus.set(finalI, -1);
                    } else {
                        trackerStatus.set(finalI, 0);
                    }

                } catch (Exception e) {
                    trackerStatus.set(finalI, -1);
                }
            });
        }
    }
}
