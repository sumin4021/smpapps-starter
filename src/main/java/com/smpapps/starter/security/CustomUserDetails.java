package com.smpapps.starter.security;

import com.smpapps.starter.users.model.User;

import lombok.Getter;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
  public CustomUserDetails(User user) {
    super(user.getEmail(), user.getPassword(), user.getUserAuthorities());
  }
}
