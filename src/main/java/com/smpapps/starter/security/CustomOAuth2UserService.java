package com.smpapps.starter.security;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.smpapps.starter.users.model.User;
import com.smpapps.starter.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
    try {
      return processOAuth2User(oAuth2UserRequest, oAuth2User);
    } catch (Exception ex) {
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
    }
  }

  private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {

    OAuth2_UserInfo oAuth2UserInfo;
    if (oAuth2UserRequest.getClientRegistration().getRegistrationId().equals("naver")) {
      oAuth2UserInfo = new OAuth2_NaverUserInfo(oAuth2User.getAttributes());
    } else if (oAuth2UserRequest.getClientRegistration().getRegistrationId().equals("google")) {
      oAuth2UserInfo = new OAuth2_GoogleUserInfo(oAuth2User.getAttributes());
    } else {
      throw new OAuth2AuthenticationException("Sorry! Login with " + oAuth2UserRequest.getClientRegistration().getRegistrationId() + " is not supported yet.");
    }

    log.info("SMP :::::::: {}", oAuth2UserInfo.getEmail());

    User user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
        .map(existingUser -> {
          // 기존 사용자 정보 업데이트 (필요한 경우)
          existingUser.setName(oAuth2UserInfo.getName());
          return userRepository.save(existingUser);
        })
        .orElseGet(() -> {
          User newUser = User.builder()
              .email(oAuth2UserInfo.getEmail())
              .password(new BCryptPasswordEncoder().encode("issnsjoinuser!@$sdasd"))
              .name(oAuth2UserInfo.getName())
              .authority("ROLE_NORMAL")
              .build();
          return userRepository.save(newUser);
        });

    return new CustomUserDetails(user, oAuth2User.getAttributes());
  }
}
