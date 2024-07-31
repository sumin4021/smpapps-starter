package com.smpapps.starter.security;

import java.util.Map;
import java.util.Optional;

import lombok.Getter;

@Getter
@SuppressWarnings("unchecked")
public class OAuth2_AppleUserInfo implements OAuth2_UserInfo {

  private final Map<String, Object> attributes;
  private final Map<String, Object> response;

  public OAuth2_AppleUserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
    this.response = (Map<String, Object>) attributes.get("response");
  }

  private Optional<String> getResponseValue(String key) {
    return Optional.ofNullable(response).map(r -> (String) r.get(key));
  }

  @Override
  public String getId() {
    return getResponseValue("id").orElse("");
  }

  @Override
  public String getName() {
    return getResponseValue("name").orElse("");
  }

  @Override
  public String getEmail() {
    return getResponseValue("email").orElse("");
  }

  @Override
  public String getProfileImage() {
    return "";
  }
}