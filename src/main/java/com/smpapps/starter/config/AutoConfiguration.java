package com.smpapps.starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.smpapps.starter.security.CustomOAuth2UserService;
import com.smpapps.starter.users.repository.UserRepository;

@Configuration
@EnableJpaRepositories(basePackages = "com.smpapps.starter")
@EntityScan(basePackages = "com.smpapps.starter")
public class AutoConfiguration {

  @Configuration
  @ConditionalOnMissingBean({
      MultipartAutoConfiguration.class,
      ThymeleafSecurityAutoConfiguration.class
  })
  @Import(DataSourceAutoConfiguration.class)
  public static class ConditionalSecurityConfiguration {
    
    @Bean
    CustomOAuth2UserService customOAuth2UserService(UserRepository userRepository) {
      return new CustomOAuth2UserService(userRepository);
    }

    @Bean
    MultipartAutoConfiguration multipartAutoConfiguration() {
      return new MultipartAutoConfiguration();
    }

    @Bean
    ThymeleafSecurityAutoConfiguration thymeleafSecurityAutoConfiguration() {
      return new ThymeleafSecurityAutoConfiguration();
    }

  }
}