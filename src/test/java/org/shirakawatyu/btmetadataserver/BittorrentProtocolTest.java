package org.shirakawatyu.btmetadataserver;

import org.junit.jupiter.api.Test;
import org.shirakawatyu.btmetadataserver.pojo.TrackerReq;
import org.shirakawatyu.btmetadataserver.bittorrent.MetaInfoApi;
import org.shirakawatyu.btmetadataserver.bittorrent.TrackerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class BittorrentProtocolTest {
    @Autowired
    TrackerApi trackerApi;
    @Autowired
    MetaInfoApi metaInfoApi;

    @Test
    void test() {
        String tracker = "http://tracker.skyts.net:6969/announce";
        String peerId = "-TR4060-" + "123456789012";
        TrackerReq req = new TrackerReq("d4358578d53e37268de6123b824b1d6753e5bf74", peerId, "61317");
        Map<String, Object> stringObjectMap = metaInfoApi.getMetaInfo("d4358578d53e37268de6123b824b1d6753e5bf74", trackerApi.requestTracker(req), peerId).getInfo();
        System.out.println(stringObjectMap);
    }
}
