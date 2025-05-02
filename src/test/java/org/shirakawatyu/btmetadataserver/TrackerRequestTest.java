package org.shirakawatyu.btmetadataserver;

import org.junit.jupiter.api.Test;
import org.shirakawatyu.btmetadataserver.pojo.Peer;
import org.shirakawatyu.btmetadataserver.pojo.TrackerReq;
import org.shirakawatyu.btmetadataserver.bittorrent.TrackerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TrackerRequestTest {
    @Autowired
    TrackerApi trackerApi;

    @Test
    void requestTracker() {
        String tracker = "http://tracker.opentrackr.org:1337/announce";
        String peerId = "-TR4060-" + "123456789012";
        TrackerReq req = new TrackerReq("6c7afac67703dacf229d2ef64688fca966403488", peerId, "61317");
        List<Peer> resp = trackerApi.requestTrackerSingle(tracker, req);
        System.out.println(resp);
    }

    @Test
    void bytesToPeersTest() {
        byte[] peers = new byte[6];
        int[] ip = {192, 168, 39, 253};
        int port = 61413;
        for (int i = 0; i < 4; i++) {
            peers[i] = (byte) ip[i];
        }
        peers[4] = (byte) ((port >> 8) & 0xFF);
        peers[5] = (byte) (port & 0xFF);
        List<Peer> peers1 = trackerApi.bytesToPeers(peers);
        System.out.println(peers1);
    }
}
