package org.shirakawatyu.btmetadataserver.pojo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "bittorrent")
@Component
public class BittorrentConfig {
    private List<String> trackerList;
    private Proxy proxy;
    private Rss rss;

    @Data
    public static class Proxy {
        String ip;
        int port;
        boolean enable;
    }

    @Data
    public static class Rss {
        String url;
        String cron;
        int concurrency;
    }
}
