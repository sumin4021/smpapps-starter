package com.smpapps.starter.security;

import java.util.Map;

import lombok.Getter;

@Getter
@SuppressWarnings("unchecked")
public class OAuth2_NaverUserInfo implements OAuth2_UserInfo {

  private Map<String, Object> attributes;

  public OAuth2_NaverUserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public String getId() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
    if (response == null) {
      return null;
    }
    return (String) response.get("id");
  }

  public String getName() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
    if (response == null) {
      return null;
    }
    return (String) response.get("name");
  }

  public String getEmail() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
    if (response == null) {
      return null;
    }
    return (String) response.get("email");
  }
}
