package com.smpapps.starter.social.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "starter.social")
public class SocialLoginProperty {

  private NaverProperties naver;
  private KakaoProperties kakao;

  @Data
  public class NaverProperties {
    private String clientId;
    private String clientSecret;
    private String callback;
  }

  @Data
  public class KakaoProperties {
    private String clientId;
    private String clientSecret;
    private String callback;
  }

  public SocialLoginProperty() {
    this.naver = new NaverProperties();
    this.kakao = new KakaoProperties();
  }
}
