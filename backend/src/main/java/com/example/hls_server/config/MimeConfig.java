package com.example.hls_server.config;

import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MimeConfig {
    @Bean
    public ConfigurableWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setMimeMappings(customMimeMappings());
        return factory;
    }

    private MimeMappings customMimeMappings() {
        MimeMappings m = new MimeMappings(MimeMappings.DEFAULT);
        m.add("m3u8", "application/vnd.apple.mpegurl");
        m.add("m3u", "application/x-mpegURL");
        m.add("ts", "video/mp2t");
        m.add("m4s", "video/iso.segment");
        return m;
    }
}
