package org.shirakawatyu.btmetadataserver.util;


import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
public class RequestUtil {
    private static OkHttpClient client;

    public static byte[] getForBytes(String url) {
        try {
            return get(url, ReturnType.BYTES);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String getForString(String url) {
        try {
            return get(url, ReturnType.STRING);
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String url, ReturnType type) {
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (type == ReturnType.STRING) {
                return (T) response.body().string();
            } else if (type == ReturnType.BYTES) {
                return (T) response.body().bytes();
            }
        } catch (IOException e) {
            log.error("{} occurred while requesting url: {}", e.getMessage(), url);
            return null;
        }
        return null;
    }

    @Component
    public static class InnerInjector {
        @Autowired
        public void setClient(OkHttpClient client) {
            RequestUtil.client = client;
        }
    }

    public enum ReturnType {
        STRING,
        BYTES
    }
}
