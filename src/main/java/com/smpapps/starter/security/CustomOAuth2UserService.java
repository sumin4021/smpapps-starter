package com.smpapps.starter.security;

import java.time.LocalDateTime;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class CustomOAuth2UserService extends DefaultOAuth2UserService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

    log.debug("oAuth2UserRequest : {} ", oAuth2UserRequest);

    try {
      return processOAuth2User(oAuth2UserRequest, oAuth2User);
    } catch (Exception ex) {
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    String[] parts = username.split(":");
    if (parts.length != 2) {
      throw new UsernameNotFoundException("Invalid username format");
    }

    String email = parts[0];
    String joinChannel = parts[1];

    User user = userRepository.findByEmailAndJoinChannel(email, joinChannel)
        .orElseThrow(() -> new UsernameNotFoundException(
            "User not found with email: " + email + " and joinChannel: " + joinChannel));

    return new CustomUserDetails(user);
  }

  private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
    OAuth2_UserInfo oAuth2UserInfo;
    String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

    log.debug("login id type : {} ", registrationId);

    switch (registrationId) {
      case "naver":
        oAuth2UserInfo = new OAuth2_NaverUserInfo(oAuth2User.getAttributes());
        break;
      case "google":
        oAuth2UserInfo = new OAuth2_GoogleUserInfo(oAuth2User.getAttributes());
        break;
      case "facebook":
        oAuth2UserInfo = new OAuth2_FacebookUserInfo(oAuth2User.getAttributes());
        break;
      case "instagram":
        oAuth2UserInfo = new OAuth2_InstagramUserInfo(oAuth2User.getAttributes());
        break;
      default:
        throw new OAuth2AuthenticationException("Sorry! Login with " + registrationId + " is not supported yet.");
    }

    log.debug("OAuth2User channel: {} name: {} email: {} ", registrationId, oAuth2UserInfo.getName(),
        oAuth2UserInfo.getEmail());

    User user = userRepository.findByEmailAndJoinChannel(oAuth2UserInfo.getEmail(), registrationId)
        .map(existingUser -> {
          existingUser.setName(oAuth2UserInfo.getName());
          existingUser.setLoginDate(LocalDateTime.now());
          return userRepository.save(existingUser);
        })
        .orElseGet(() -> {
          User newUser = User.builder()
              .email(oAuth2UserInfo.getEmail())
              .password(new BCryptPasswordEncoder().encode("issnsjoinuser!@$sdasd"))
              .name(oAuth2UserInfo.getName())
              .profileImage(oAuth2UserInfo.getProfileImage())
              .joinChannel(registrationId)
              .authority("ROLE_NORMAL")
              .joinDate(LocalDateTime.now())
              .loginDate(LocalDateTime.now())
              .build();
          return userRepository.save(newUser);
        });

    return new CustomUserDetails(user, oAuth2User.getAttributes());
  }
}