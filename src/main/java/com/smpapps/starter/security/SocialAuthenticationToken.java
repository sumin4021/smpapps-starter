package com.smpapps.starter.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import lombok.Getter;

@Getter
public class SocialAuthenticationToken extends AbstractAuthenticationToken {

  private final Object principal;
  private Object credentials;
  private String joinChannel; // 소셜 로그인 경로 (예: G, F, A)

  public SocialAuthenticationToken(Object principal, Object credentials, String joinWay) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    this.joinChannel = joinWay;
    setAuthenticated(false);
  }

  public SocialAuthenticationToken(Object principal, Object credentials, String joinWay, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    this.joinChannel = joinWay;
    super.setAuthenticated(true);
  }

  @Override
  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
    super.setAuthenticated(false);
  }

  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
    this.credentials = null;
  }
}
