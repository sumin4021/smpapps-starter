package com.smpapps.starter.social.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_token")
public class Token {

  @OneToOne
  @JoinColumn(name = "user_sns_id")
  private SnsInfo snsInfo;

  @Id
  @Column(nullable = false, unique = true, name = "user_token_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long tokenId;

  private String access_token; // string 접근 토큰, 발급 후 expires_in 파라미터에 설정된 시간(초)이 지나면 만료됨
  private String refresh_token; // string 갱신 토큰, 접근 토큰이 만료될 경우 접근 토큰을 다시 발급받을 때 사용
  private String token_type; // string 접근 토큰의 타입으로 Bearer와 MAC의 두 가지를 지원
  private int expires_in; // integer 접근 토큰의 유효 기간(초 단위)
  private String error; // string error code
  private String error_description; // string description
}
