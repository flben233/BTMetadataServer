package org.shirakawatyu.btmetadataserver.service;

import java.util.Map;

public interface MetadataService {
    Map<String, Object> getMetadata(String infoHash);

    byte[] getTorrent(String infoHash);
}
