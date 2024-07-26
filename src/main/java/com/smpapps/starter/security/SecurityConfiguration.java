package com.smpapps.starter.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

  // private final CustomUserDetailsService customUserDetailsService;
  // private final SocialAuthenticationProvider socialAuthenticationProvider;
  // private final DataSource dataSource;

  @Bean
  BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // @Bean
  // DaoAuthenticationProvider daoAuthenticationProvider() {
  //   DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
  //   provider.setUserDetailsService(customUserDetailsService);
  //   provider.setPasswordEncoder(passwordEncoder());
  //   return provider;
  // }

  // @Bean
  // AuthenticationManager authManager(HttpSecurity http) throws Exception {
  //   AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
  //   builder.authenticationProvider(daoAuthenticationProvider());
  //   builder.authenticationProvider(socialAuthenticationProvider);
  //   return builder.build();
  // }

  // @Bean
  // PersistentTokenRepository persistentTokenRepository() {
  //   JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
  //   repo.setDataSource(dataSource);
  //   return repo;
  // }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers.frameOptions(Customizer.withDefaults()))
        .authorizeHttpRequests(authRequest -> authRequest
            .requestMatchers(HttpMethod.HEAD, "/**").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/comments").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/boards").permitAll()
            .requestMatchers(HttpMethod.POST, "/**").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/v1/imgs/{id}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/**").denyAll()
            .requestMatchers(HttpMethod.GET, "/**").permitAll()
            .anyRequest().authenticated())
        .formLogin(formLogin -> formLogin
            .loginPage("/login")
            .defaultSuccessUrl("/", true)
            .usernameParameter("email")
            .passwordParameter("password")
            .successHandler((request, response, authentication) -> {
              HttpSession session = request.getSession(true);
              UserDetails userDetails = (UserDetails) authentication.getPrincipal();
              session.setAttribute("USER_EMAIL", userDetails.getUsername());
              session.setAttribute("USER_ROLES", userDetails.getAuthorities());
              if (userDetails instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
                session.setAttribute("USER_ID", customUserDetails.getId());
                session.setAttribute("USER_NAME", customUserDetails.getName());
              }
              response.sendRedirect("/");
            })
            .failureHandler((request, response, exception) -> {
              String errorMessage = "Unknown error";
              if (exception instanceof BadCredentialsException) {
                errorMessage = "invalid_credentials";
              } else if (exception instanceof LockedException) {
                errorMessage = "account_locked";
              } else if (exception instanceof DisabledException) {
                errorMessage = "account_disabled";
              } else if (exception instanceof AccountExpiredException) {
                errorMessage = "account_expired";
              } else if (exception instanceof CredentialsExpiredException) {
                errorMessage = "credentials_expired";
              }
              response.sendRedirect("/login?fail=" + errorMessage);
            })
            .permitAll())
        .logout(logout -> logout
            .logoutUrl("/logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "remember-me")
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
            .logoutSuccessUrl("/login?logout")
            .permitAll())
        .rememberMe(rememberme -> rememberme
            .key("smp!@#rememberme!A)KC99Key")
            .tokenValiditySeconds(60 * 60 * 24 * 30))
        .sessionManagement(session -> session
            .sessionFixation().changeSessionId()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
            .expiredUrl("/login?fail=expire_session"))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.sendRedirect("/block");
            }));
    return http.build();
  }
}