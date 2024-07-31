package com.smpapps.starter.security;

import java.util.Map;

import lombok.Getter;

@Getter
public class OAuth2_GoogleUserInfo implements OAuth2_UserInfo {

  private Map<String, Object> attributes;

  public OAuth2_GoogleUserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String getId() {
    return (String) attributes.get("sub");
  }

  @Override
  public String getName() {
    return (String) attributes.get("name");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }

  @Override
  public String getProfileImage() {
    return (String) attributes.get("picture");
  }

}
