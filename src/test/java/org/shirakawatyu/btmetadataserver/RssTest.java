package org.shirakawatyu.btmetadataserver;

import org.junit.jupiter.api.Test;
import org.shirakawatyu.btmetadataserver.service.MetadataService;
import org.shirakawatyu.btmetadataserver.service.impl.MetadataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RssTest {
    @Autowired
    MetadataServiceImpl metadataService;

    @Test
    void test() {
        metadataService.updateMetadata();
    }
}
