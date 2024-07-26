package com.smpapps.starter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.smpapps.starter.security.SecurityConfiguration;

@Configuration
@Import({
    SecurityConfiguration.class,
    MultipartAutoConfiguration.class,
    ThymeleafSecurityAutoConfiguration.class
})
public class AutoConfiguration {
}