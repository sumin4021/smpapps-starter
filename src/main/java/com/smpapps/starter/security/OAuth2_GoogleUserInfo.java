package com.smpapps.starter.security;

import java.util.Map;

import lombok.Getter;

@Getter
public class OAuth2_GoogleUserInfo implements OAuth2_UserInfo {
  
  private Map<String, Object> attributes;

  public OAuth2_GoogleUserInfo(Map<String, Object> attributes) {
      this.attributes = attributes;
  }

  public String getId() {
      return (String) attributes.get("sub");
  }

  public String getName() {
      return (String) attributes.get("name");
  }

  public String getEmail() {
      return (String) attributes.get("email");
  }
}
