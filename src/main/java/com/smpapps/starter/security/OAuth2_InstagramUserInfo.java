package com.smpapps.starter.security;

import java.util.Map;

import lombok.Getter;

@Getter
public class OAuth2_InstagramUserInfo implements OAuth2_UserInfo {

  private Map<String, Object> attributes;

  public OAuth2_InstagramUserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String getId() {
    return (String) attributes.get("id");
  }

  @Override
  public String getName() {
    return (String) attributes.get("username");
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("username")+"@instagram.com";
  }

  @Override
  public String getProfileImage() {
    return "";
  }

}