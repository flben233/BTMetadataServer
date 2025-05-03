package org.shirakawatyu.btmetadataserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(basePackages = "org.shirakawatyu.btmetadataserver.mapper")
@EnableScheduling
public class BtMetadataServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BtMetadataServerApplication.class, args);
    }

}
