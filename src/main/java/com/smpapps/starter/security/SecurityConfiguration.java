package com.smpapps.starter.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
    // repo.setCreateTableOnStartup(true); // true면 무조건 만듬
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
            .requestMatchers("/login", "/logout", "/oauth2/**", "/error").permitAll()
            .requestMatchers(HttpMethod.HEAD, "/**").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/**").authenticated()
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
            .clearAuthentication(true)
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID", "remember-me")
            .logoutSuccessUrl("/")
            .permitAll())
        .rememberMe(rememberme -> rememberme
            .key("smp!@#rememberme!A)KC99Key")
            .tokenValiditySeconds(60 * 60 * 24 * 30)
            .tokenRepository(persistentTokenRepository()))
        .sessionManagement(session -> session
            .sessionFixation().changeSessionId()
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
            .expiredUrl("/login?fail=expire_session"))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.sendRedirect("/block");
            }))
        .oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            // .defaultSuccessUrl("/", true)
            .successHandler((request, response, authentication) -> {
              CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
              HttpSession session = request.getSession();
              session.setAttribute("username", userDetails.getName());
              response.sendRedirect("/");
            })
            .failureUrl("/login?fail=oauth_error")
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService)));

    return http.build();
  }
}