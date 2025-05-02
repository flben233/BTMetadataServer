package org.shirakawatyu.btmetadataserver.bittorrent;

import lombok.extern.slf4j.Slf4j;
import org.shirakawatyu.btmetadataserver.pojo.BittorrentConfig;
import org.shirakawatyu.btmetadataserver.pojo.Metadata;
import org.shirakawatyu.btmetadataserver.pojo.Peer;
import org.shirakawatyu.btmetadataserver.util.ArrayUtil;
import org.shirakawatyu.btmetadataserver.util.BinaryUtil;
import org.shirakawatyu.btmetadataserver.util.EncodeUtil;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.FutureTask;


@Slf4j
@Component
@SuppressWarnings("unchecked")
public class MetaInfoApi {
    private final List<String> trackerList;

    public MetaInfoApi(BittorrentConfig bittorrentConfig) {
        this.trackerList = bittorrentConfig.getTrackerList();
    }

    private byte[] getHandshakeBody(String hashInfo, String peerId) {
        byte[] protocolNameLength = {19};
        byte[] protocolName = "BitTorrent protocol".getBytes(StandardCharsets.UTF_8);
        byte[] reserved = {0, 0, 0, 0, 0, 18, 0, 5};
        byte[] hashInfoBytes = EncodeUtil.hexToBytes(hashInfo);
        byte[] peerIdBytes = peerId.getBytes(StandardCharsets.UTF_8);
        return ArrayUtil.concatBytes(protocolNameLength, protocolName, reserved, hashInfoBytes, peerIdBytes);
    }

    private byte[] getMsgBody(byte[] body, byte msgId) {
        byte[] msgType = {20};
        byte[] msgIdBytes = {msgId};
        byte[] msgLength = BinaryUtil.intToBytes(body.length + 2);
        return ArrayUtil.concatBytes(msgLength, msgType, msgIdBytes, body);
    }

    private byte[] getExtendedBody(int port, byte msgId, String ip, int pieceIndex) {
        byte[] ipBytes = BinaryUtil.ipToBytes(ip);
        byte[] msgBody1Parts1 = ("d12:complete_agoi-1e" +
                "1:md11:lt_donthavei7e10:share_modei8e11:upload_onlyi3e12:ut_holepunchi4e" +
                "11:ut_metadatai" + msgId + "ee" +
                "1:pi" + port + "e" +
                "4:reqqi500e1:v15:FDM/6.19.0.51566:yourip4:").getBytes(StandardCharsets.UTF_8);
        byte[] msgBody1Parts2 = "e".getBytes(StandardCharsets.UTF_8);
        byte[] msgBody1 = ArrayUtil.concatBytes(msgBody1Parts1, ipBytes, msgBody1Parts2);
        byte[] msgBody2 = ("d8:msg_typei0e5:piecei" + pieceIndex + "ee").getBytes(StandardCharsets.UTF_8);
        byte[] msgBytes1 = getMsgBody(msgBody1, (byte) 0);
        byte[] msgBytes2 = getMsgBody(msgBody2, msgId);
        return ArrayUtil.concatBytes(msgBytes1, msgBytes2);
    }

    private byte[] handshake(String hashInfo, Socket peerSocket, String peerId) throws IOException {
        byte[] handshakeBody = getHandshakeBody(hashInfo, peerId);
        peerSocket.getOutputStream().write(handshakeBody);
        peerSocket.getOutputStream().flush();
        return peerSocket.getInputStream().readNBytes(handshakeBody.length);
    }

    private byte[] extended(Socket peerSocket) throws IOException {
        InputStream pis = peerSocket.getInputStream();
        OutputStream pos = peerSocket.getOutputStream();
        // 握手消息预处理
        byte[] msgLengthBytes, msgType, id, msgBody = new byte[0];
        int msgLength;
        do {
            msgLengthBytes = pis.readNBytes(4);
            msgLength = BinaryUtil.bytesToInt(msgLengthBytes);
            msgType = pis.readNBytes(1);
            if (msgLength - 1 > 0) {
                id = pis.readNBytes(1);
                // 消息体长度为msgLength - 2，因为前面有两个字节表示类型和id
                msgBody = pis.readNBytes(msgLength - 2);
            }
        } while (msgType[0] != 20);

        BencodeParser parser = new BencodeParser(msgBody);
        int metadataSize;
        byte[] extBytes;
        byte metaMsgId;
        Map<String, Object> dict = (Map<String, Object>) parser.parse();
        Map<String, Object> m = (Map<String, Object>) dict.get("m");
        if (!m.containsKey("ut_metadata")) {
            log.info("Peer {} does not support metadata protocol", peerSocket.getInetAddress());
            throw new RuntimeException("Peer does not support metadata protocol");
        }
        metaMsgId = ((Long) m.get("ut_metadata")).byteValue();
        metadataSize = ((Long) dict.get("metadata_size")).intValue();

        // 接收数据
        int read = 0;
        byte[] dataMsg = new byte[metadataSize];
        double numBlocks = Math.ceil(metadataSize / 16384.0);
        pis = new BufferedInputStream(pis);
        for (int i = 0; i < numBlocks; i++) {
            extBytes = getExtendedBody(peerSocket.getLocalPort(), metaMsgId, peerSocket.getLocalAddress().getHostAddress(), i);
            // 发送扩展消息
            pos.write(extBytes);
            pos.flush();
        }
        while (read < metadataSize) {
            // 接收数据消息
            byte[] bytes = pis.readNBytes(4);
            int bytesRead = BinaryUtil.bytesToInt(bytes);
            msgType = pis.readNBytes(1);
            byte[] data = pis.readNBytes(bytesRead - 1);
            if (msgType[0] != 20) {
                continue;
            }
            // 查找消息头的末尾，特征为ee
            int start;
            for (start = 0; start < data.length - 1; start++) {
                if (data[start] == 101 && data[start + 1] == 101) {
                    break;
                }
            }
            start += 2;
            System.arraycopy(data, start, dataMsg, read, bytesRead - start - 1);
            read += bytesRead - start - 1;
        }
        return dataMsg;
    }

    public byte[] constructTorrent(byte[] info) {
        String tracker = trackerList.getFirst();
        String torrent = "d8:announce" + tracker.length() + ":" + tracker + "4:info";
        return ArrayUtil.concatBytes(torrent.getBytes(StandardCharsets.UTF_8), info, "e".getBytes(StandardCharsets.UTF_8));
    }

    public Metadata getMetaInfo(String infoHash, List<Peer> peers, String peerId) {
        Metadata metadata = null;
        List<FutureTask<Socket>> futureTasks = new ArrayList<>();
        for (Peer peer : peers) {
            FutureTask<Socket> task = new FutureTask<>(() -> {
                try {
                    Socket socket = new Socket(peer.ip(), peer.port());
                    socket.setSoTimeout(15000);
                    return socket;
                } catch (Exception e) {
                    return null;
                }
            });
            futureTasks.add(task);
            Thread.startVirtualThread(task);
        }
        for (FutureTask<Socket> t : futureTasks) {
            try (Socket peerSocket = t.get()) {
                if (peerSocket == null) {
                    continue;
                }
                if (metadata == null) {
                    byte[] handshake = handshake(infoHash, peerSocket, peerId);
                    if (handshake.length == 0) {
                        continue;
                    }
                    byte[] ext = extended(peerSocket);
                    Map<String, Object> infoMap = (Map<String, Object>) new BencodeParser(ext).parse();
                    infoMap.remove("pieces");
                    metadata = new Metadata(infoHash, infoMap, constructTorrent(ext));
                }
            } catch (SocketException | SocketTimeoutException ignored) {

            } catch (RuntimeException e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                log.error("Error connecting to peer", e);
            }
        }
        return metadata;
    }
}
