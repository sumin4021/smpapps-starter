package com.smpapps.starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.smpapps.starter.security.SocialAuthenticationProvider;

@Configuration
public class AuthenticationConfig {
  @Bean
  @ConditionalOnMissingBean(UserDetailsService.class)
  SocialAuthenticationProvider socialAuthenticationProvider(UserDetailsService userDetailsService) {
    return new SocialAuthenticationProvider(userDetailsService);
  }
}