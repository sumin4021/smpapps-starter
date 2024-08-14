package com.smpapps.starter.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.smpapps.starter.users.model.User;

public class CustomUserDetails implements UserDetails, OAuth2User {

  private final User user;
  private Map<String, Object> attributes;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  public CustomUserDetails(User user, Map<String, Object> attributes) {
    this.user = user;
    this.attributes = attributes;
  }

  public User getUser() {
    return this.user;
  }
  
  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return user.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return user.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return user.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getUserAuthorities();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return user.getName();
  }

  // 추가 메서드
  public Long getId() {
    return user.getId();
  }

  public String getEmail() {
    return user.getEmail();
  }

  public String getProfileImage() {
    return user.getProfileImage();
  }

  public String getJoinChannel() {
    return user.getJoinChannel();
  }

}