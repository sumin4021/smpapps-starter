package com.smpapps.starter.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocialAuthenticationProvider implements AuthenticationProvider {

  private final UserDetailsService userDetailsService;

  @Override
  public boolean supports(Class<?> authentication) {
    return SocialAuthenticationToken.class.isAssignableFrom(authentication);
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    if (!supports(authentication.getClass())) {
      return null;
    }

    SocialAuthenticationToken authToken = (SocialAuthenticationToken) authentication;
    String email = authToken.getPrincipal().toString();
    String joinChannel = authToken.getJoinChannel();

    try {
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      if (!(userDetails instanceof CustomUserDetails)) {
        throw new AuthenticationServiceException("Unexpected user details type");
      }

      String providedPassword = authToken.getCredentials().toString();
      if (!new BCryptPasswordEncoder().matches(providedPassword, userDetails.getPassword())) {
        log.warn("Failed login attempt for user: {}", email);
        throw new BadCredentialsException("패스워드가 일치하지 않습니다.");
      }

      log.info("Successful social authentication for user: {}", email);

      // 인증 성공 후 민감한 정보 삭제
      authToken.eraseCredentials();

      return new SocialAuthenticationToken(userDetails, null, joinChannel, userDetails.getAuthorities());
    } catch (UsernameNotFoundException e) {
      log.warn("User not found: {}", email);
      throw new AuthenticationServiceException("사용자를 찾을 수 없습니다.", e);
    } catch (Exception e) {
      log.error("Unexpected error during authentication", e);
      throw new AuthenticationServiceException("An unexpected error occurred", e);
    }
  }
}