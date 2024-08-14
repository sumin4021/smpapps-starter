package com.smpapps.starter.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final DataSource dataSource;

  public SecurityConfiguration(CustomOAuth2UserService customOAuth2UserService, DataSource dataSource) {
    this.customOAuth2UserService = customOAuth2UserService;
    this.dataSource = dataSource;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PersistentTokenRepository persistentTokenRepository() {
    JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
    repo.setDataSource(dataSource);
    return repo;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(Customizer.withDefaults()))
        .authorizeHttpRequests(authRequest -> authRequest
            .requestMatchers("/", "/css/**", "/images/**", "/js/**").permitAll()
            .requestMatchers("/signin", "/signout", "/oauth2/**", "/error").permitAll()
            .requestMatchers(HttpMethod.HEAD, "/**").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/**").authenticated()
            .requestMatchers(HttpMethod.GET, "/**").permitAll()
            .anyRequest().authenticated())
        .formLogin(formLogin -> formLogin
            .loginPage("/signin")
            .defaultSuccessUrl("/", true)
            .usernameParameter("email")
            .passwordParameter("password")
            .successHandler((request, response, authentication) -> {
              HttpSession session = request.getSession(true);
              UserDetails userDetails = (UserDetails) authentication.getPrincipal();
              session.setAttribute("USER_EMAIL", userDetails.getUsername());
              session.setAttribute("USER_ROLES", userDetails.getAuthorities());
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
              response.sendRedirect("/signin?fail=" + errorMessage);
            })
            .permitAll())
        .logout(logout -> logout
            .logoutUrl("/signout")
            .clearAuthentication(true)
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "remember-me")
            .logoutSuccessUrl("/")
            .permitAll())
        .rememberMe(rememberme -> rememberme
            .key("smp!@#rememberme!A)KC99Key")
            .tokenValiditySeconds(60 * 60 * 24 * 30)
            .tokenRepository(persistentTokenRepository())
            .userDetailsService(customOAuth2UserService)
            .rememberMeParameter("remember-me"))
        .sessionManagement(session -> session
            .sessionFixation().changeSessionId()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
            .expiredUrl("/signin?fail=expire_session"))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.sendRedirect("/block");
            }))
        .oauth2Login(oauth2 -> oauth2
            .loginPage("/signin")
            .successHandler((request, response, authentication) -> {
              CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
              HttpSession session = request.getSession();
              session.setAttribute("userprofileimage", userDetails.getProfileImage());
              session.setAttribute("username", userDetails.getName());
              String rememberMeToken = userDetails.getUsername() + ":" + userDetails.getJoinChannel();
              request.setAttribute("remember-me", rememberMeToken);
              response.sendRedirect("/");
            })
            .failureHandler((request, response, exception) -> {

              log.error("EXCEPTION ::::", exception);
              HttpSession session = request.getSession(false);
              if (session != null)
                session.invalidate();
              response.sendRedirect("/?fail=oauth_error");
            })
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)));

    return http.build();
  }
}