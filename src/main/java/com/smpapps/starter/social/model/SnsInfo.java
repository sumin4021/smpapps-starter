package com.smpapps.starter.social.model;

import java.io.Serializable;

import com.smpapps.starter.users.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_sns")
public class SnsInfo {

  @OneToOne
  @JoinColumn(name = "user_id")
  private User member;

  @Id
  @Column(nullable = false, unique = true, name = "user_sns_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long snsInfoId;

  @Transient // DB 에는 안생김.
  private String resultcode; // API 호출 결과 코드

  @Transient
  private String message; // 호출 결과 메시지

  @Embedded
  private Response response; // 호출 결과

  @Data
  @Embeddable
  public static class Response implements Serializable {
    private String id; // 동일인 식별 정보
    private String nickname; // 사용자 별명
    private String name; // 사용자 이름
    private String email; // 사용자 메일 주소
    private String gender; // 성별 (F: 여성, M: 남성, U: 확인불가)
    private String age; // 사용자 연령대
    private String birthday; // 사용자 생일(MM-DD 형식)
    private String birthyear; // 출생연도
    private String mobile; // 휴대전화번호
  }
}
