package org.shirakawatyu.btmetadataserver;

import org.junit.jupiter.api.Test;
import org.shirakawatyu.btmetadataserver.bittorrent.BencodeParser;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootTest
public class BencodeTest {

    @Test
    void test() {
        String str = "d12:complete_agoi970e1:md11:lt_donthavei7e10:share_modei8e11:upload_onlyi3e12:ut_holepunchi4e11:ut_metadatai2e6:ut_pexi1ee13:metadata_sizei3639e4:reqqi2000e11:upload_onlyi1e1:v17:qBittorrent/5.0.46:yourip4:¶x3e";
        BencodeParser bencodeParser = new BencodeParser(str.getBytes(StandardCharsets.UTF_8));
        try {
            Object parse = bencodeParser.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
