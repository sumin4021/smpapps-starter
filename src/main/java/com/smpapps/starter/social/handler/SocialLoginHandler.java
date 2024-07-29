package com.smpapps.starter.social.handler;

import java.util.ArrayList;
import java.util.Collections;

import javax.security.auth.login.LoginException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smpapps.starter.security.SocialAuthenticationToken;
import com.smpapps.starter.social.model.SnsInfo;
import com.smpapps.starter.social.model.Token;
import com.smpapps.starter.social.repository.SnsInfoRepository;
import com.smpapps.starter.social.repository.TokenRepository;
import com.smpapps.starter.users.model.User;
import com.smpapps.starter.users.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Controller
@Slf4j
public class SocialLoginHandler {

  private final SocialLoginUtils socialLoginUtils;
  private final TokenRepository tokenRepository;
  private final SnsInfoRepository snsInfoRepository;
  private final UserRepository userRepository;
  private final AuthenticationManager authManager;

  @GetMapping(value = "/social/callback/naver")
  public ModelAndView naverCallback(HttpServletRequest request, RedirectAttributes redirectAttributes,
      ModelAndView mv) {

    try {
      String error = request.getParameter("error");
      String error_description = request.getParameter("error_description");
      if (error != null && error.length() > 0) {
        throw new LoginException(error_description);
      }

      // 1. 토큰발행.
      Token naverToken = socialLoginUtils.getTokenNaver(request);
      // 2. 토큰 유효성 검증 및 재발급
      if (!socialLoginUtils.verifyToken(naverToken)) {
        naverToken = socialLoginUtils.refreshToken(naverToken);
      }
      // 2-1. effectively final
      final Token finalNaverToken = naverToken;
      // 3. 사용자 정보 조회
      SnsInfo snsInfoFromNaver = socialLoginUtils.getProfile(naverToken);

      SnsInfo snsInfo = snsInfoRepository.findByResponse_IdEquals(snsInfoFromNaver.getResponse().getId())
          .orElseGet(() -> {
            User user = User.builder()
                .email(snsInfoFromNaver.getResponse().getEmail())
                .authority("ROLE_NORMAL")
                .password("$2a$10$Vt5/9zMgB8kpYTJdxrRiNu.Z4wMvJOw.KIXInBhbtLzasP8Z89Yhy") // issnsjoinmember
                .build();
            userRepository.save(user);

            snsInfoFromNaver.setMember(user);
            SnsInfo newSnsInfo = snsInfoRepository.save(snsInfoFromNaver);

            finalNaverToken.setSnsInfo(newSnsInfo);
            tokenRepository.save(finalNaverToken);
            return newSnsInfo;
          });

      User user = snsInfo.getMember();
      HttpSession session = request.getSession();
      if (session.getAttribute("loginSession") != null) {
        session.removeAttribute("loginSession");
      }

      ArrayList<GrantedAuthority> authorities = new ArrayList<>(Collections.singleton(new SimpleGrantedAuthority("ROLE_NORMAL")));
      SocialAuthenticationToken authToken = new SocialAuthenticationToken(user.getEmail(), "issnsjoinmember", "N", authorities);
      Authentication authentication = authManager.authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      session.setAttribute("USER_EMAIL", userDetails.getUsername());
      session.setAttribute("USER_ROLES", userDetails.getAuthorities());

      log.debug("SOCIAL_LOGIN {} ", authToken);

    } catch (Exception e) {
      log.debug("SOCIAL_LOGIN err 4 {} ", e.getMessage());
      redirectAttributes.addAttribute("error", e.getMessage());
      mv.setViewName("redirect:/");
      return mv;
    }
    log.debug("SOCIAL_LOGIN END :::::::::::::::::::::::: ");

    mv.setViewName("redirect:/");
    return mv;
  }

}
