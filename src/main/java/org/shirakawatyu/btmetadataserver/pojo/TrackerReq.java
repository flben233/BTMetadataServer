package org.shirakawatyu.btmetadataserver.pojo;

import org.shirakawatyu.btmetadataserver.util.EncodeUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public record TrackerReq(
    String infoHash,
    String peerId,
    String port,
    String uploaded,
    String downloaded,
    String left,
    String event
) {
    public TrackerReq(String infoHash, String peerId, String port, String uploaded, String downloaded, String left) {
        this(infoHash, peerId, port, uploaded, downloaded, left, "started");
    }

    public TrackerReq(String infoHash, String peerId, String port) {
        this(infoHash, peerId, port, "0", "0", "16384", "started");
    }

    public TrackerReq(String infoHash, String peerId) {
        this(infoHash, peerId, "61317", "0", "0", "16384", "started");
    }

    public String toQueryString() {
        return "?info_hash=" + EncodeUtil.hexToURLEncode(infoHash) +
                "&peer_id=" + URLEncoder.encode(peerId, StandardCharsets.UTF_8) +
                "&port=" + port +
                "&uploaded=" + uploaded +
                "&downloaded=" + downloaded +
                "&left=" + left +
                "&event=" + event;
    }
}
