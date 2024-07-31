package com.smpapps.starter.security;

import java.util.Map;

import lombok.Getter;

@Getter
@SuppressWarnings("unchecked")
public class OAuth2_FacebookUserInfo implements OAuth2_UserInfo {

  private Map<String, Object> attributes;

  public OAuth2_FacebookUserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String getId() {
    return (String) attributes.get("id");
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
    if (attributes.containsKey("picture")) {
      Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
      if (pictureObj.containsKey("data")) {
        Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
        if (dataObj.containsKey("url")) {
          return (String) dataObj.get("url");
        }
      }
    }
    return null;
  }
}