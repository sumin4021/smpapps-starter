package com.smpapps.starter.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.smpapps.starter.security.CustomUserDetailsService;
import com.smpapps.starter.security.SecurityConfiguration;
import com.smpapps.starter.security.SocialAuthenticationProvider;
import com.smpapps.starter.users.repository.UserRepository;

@Configuration
@EnableJpaRepositories(basePackages = "com.smpapps.starter")
@EntityScan(basePackages = "com.smpapps.starter")
public class AutoConfiguration {

  @Configuration
  @ConditionalOnMissingBean({
      SecurityConfiguration.class,
      MultipartAutoConfiguration.class,
      ThymeleafSecurityAutoConfiguration.class
  })
  @Import(DataSourceAutoConfiguration.class)
  public static class ConditionalSecurityConfiguration {

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
      return new CustomUserDetailsService(userRepository);
    }

    @Bean
    SocialAuthenticationProvider socialAuthenticationProvider(UserDetailsService userDetailsService) {
      return new SocialAuthenticationProvider(userDetailsService);
    }

    @Bean
    @ConditionalOnMissingBean
    SecurityConfiguration securityConfiguration(
        CustomUserDetailsService userDetailsService,
        SocialAuthenticationProvider socialAuthenticationProvider,
        DataSource dataSource) {
      return new SecurityConfiguration(userDetailsService, socialAuthenticationProvider, dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    MultipartAutoConfiguration multipartAutoConfiguration() {
      return new MultipartAutoConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    ThymeleafSecurityAutoConfiguration thymeleafSecurityAutoConfiguration() {
      return new ThymeleafSecurityAutoConfiguration();
    }
  }
}