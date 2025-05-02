package org.shirakawatyu.btmetadataserver.config;

import org.shirakawatyu.btmetadataserver.pojo.BittorrentConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;


@Configuration
public class OkHttpConfig {
    private final Proxy proxy;

    public OkHttpConfig(BittorrentConfig config) {
        InetSocketAddress address = new InetSocketAddress(config.getProxy().getIp(), config.getProxy().getPort());
        if (config.getProxy().isEnable()) {
            this.proxy = new Proxy(Proxy.Type.SOCKS, address);
        } else {
            this.proxy = Proxy.NO_PROXY;
        }
    }

    @Bean
    public okhttp3.OkHttpClient okHttpClient() {
        return new okhttp3.OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }
}
