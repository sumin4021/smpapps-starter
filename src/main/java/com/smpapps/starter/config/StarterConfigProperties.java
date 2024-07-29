package com.smpapps.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "starter")
public class StarterConfigProperties {
    private Db db = new Db();
    private Social social = new Social();

    @Getter
    @Setter
    public static class Db {
        private String url;
        private String port;
        private String name;
    }

    @Getter
    @Setter
    public static class Social {
        private Naver naver = new Naver();

        @Getter
        @Setter
        public static class Naver {
            private String clientId;
            private String clientSecret;
            private String callback;
        }
    }
}