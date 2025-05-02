package org.shirakawatyu.btmetadataserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "org.shirakawatyu.btmetadataserver.mapper")
public class BtMetadataServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BtMetadataServerApplication.class, args);
    }

}
