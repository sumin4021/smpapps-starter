package com.smpapps.starter.social.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;

import javax.security.auth.login.LoginException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.smpapps.starter.social.model.SnsInfo;
import com.smpapps.starter.social.model.SocialLoginProperty;
import com.smpapps.starter.social.model.Token;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class SocialLoginUtils {

  private final Gson gson;
  private final SocialLoginProperty socialLoginProperty;

  /**
   * 1. 네이버 URL 생성.
   * 
   * @param request
   * @return
   */
  public String getUrlNaver(HttpServletRequest request) {
    String clientId = socialLoginProperty.getNaver().getClientId();
    SecureRandom random = new SecureRandom();
    String state = new BigInteger(130, random).toString();
    request.getSession().setAttribute("state", state);

    String callbackURL = request.getScheme() + "://" + request.getServerName()
        + socialLoginProperty.getNaver().getCallback();
    return "https://nid.naver.com/oauth2.0/authorize?response_type=code"
        + "&client_id=" + clientId
        + "&redirect_uri=" + callbackURL
        + "&state=" + state;
  }

  // https://nid.naver.com/oauth2.0/authorize?response_type=code
  // &client_id=4RVC0EXmJiXxkRdj6OG9
  // &redirect_uri=https://bemywedding.com/social/callback/naver
  // &state=39152638986487306669306503660263984842

  /**
   * 2. 네이버 ACCESS TOKEN 발행
   * 
   * @param apiURL
   * @return
   * @throws MalformedURLException
   */
  public Token getTokenNaver(HttpServletRequest request) throws Exception {
    Token token = null;
    String apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
        + "&client_id=" + socialLoginProperty.getNaver().getClientId()
        + "&client_secret=" + socialLoginProperty.getNaver().getClientSecret()
        + "&redirect_uri=" + socialLoginProperty.getNaver().getCallback()
        + "&code=" + request.getParameter("code")
        + "&state=" + request.getParameter("state");

    URL url = new URI(apiURL).toURL();
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    int responseCode = con.getResponseCode();
    if (responseCode != 200) {
      throw new ResponseStatusException(HttpStatus.valueOf(responseCode), "네이버 서비스 연결에 실패했습니다.");
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuilder res = new StringBuilder();
    while ((inputLine = br.readLine()) != null) {
      res.append(inputLine);
    }
    br.close();
    token = gson.fromJson(res.toString(), Token.class);

    if (token.getError() != null && !token.getError().equals("")) {
      throw new LoginException("소셜 로그인에 실패했습니다. " + token.getError_description());
    }
    return token;
  }

  /**
   * 3. ACCESS TOKEN 으로 사용자정보 받아오기.
   * 
   * @param naverToken
   * @return
   * @throws MalformedURLException
   */
  public SnsInfo getProfile(Token naverToken) throws Exception {
    SnsInfo snsInfo = null;
    String apiURL = "https://openapi.naver.com/v1/nid/me";
    URL url = new URI(apiURL).toURL();
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestProperty("Authorization", "Bearer " + naverToken.getAccess_token());
    con.setRequestProperty("Content-Type", "application/json");
    con.connect();
    int responseCode = con.getResponseCode();
    if (responseCode != 200) {
      throw new ResponseStatusException(HttpStatus.valueOf(responseCode), "네이버 서비스 연결에 실패했습니다.");
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuilder res = new StringBuilder();
    while ((inputLine = br.readLine()) != null) {
      res.append(inputLine);
    }
    br.close();
    snsInfo = gson.fromJson(res.toString(), SnsInfo.class);
    if (!snsInfo.getResultcode().equals("00")) {
      throw new LoginException(snsInfo.getMessage());
    }
    return snsInfo;
  }

  /**
   * ACCESS TOKEN 검증
   * 
   * @param naverToken
   * @return false > 유효하지 않음.
   */
  public boolean verifyToken(Token naverToken) throws Exception {
    String apiURL = "https://openapi.naver.com/v1/nid/verify";
    URL url = new URI(apiURL).toURL();
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestProperty("Authorization", "Bearer " + naverToken.getAccess_token());
    int responseCode = con.getResponseCode();
    if (responseCode != 200) {
      return false;
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuilder res = new StringBuilder();
    while ((inputLine = br.readLine()) != null) {
      res.append(inputLine);
    }
    br.close();
    SnsInfo snsInfo = gson.fromJson(res.toString(), SnsInfo.class);
    if (!snsInfo.getResultcode().equals("00")) {
      return false;
    }
    return true;
  }

  /**
   * ACCESS TOKEN 재발급.
   * 
   * @param naverToken
   * @return
   */
  public Token refreshToken(Token naverToken) throws Exception {
    Token token = null;
    String apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=refresh_token"
        + "&client_id=" + socialLoginProperty.getNaver().getClientId()
        + "&client_secret=" + socialLoginProperty.getNaver().getClientSecret()
        + "&refresh_token=" + naverToken.getRefresh_token();
    URL url = new URI(apiURL).toURL();
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    int responseCode = con.getResponseCode();
    if (responseCode != 200) {
      throw new ResponseStatusException(HttpStatus.valueOf(responseCode), "네이버 서비스 연결에 실패했습니다.");
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuilder res = new StringBuilder();
    while ((inputLine = br.readLine()) != null) {
      res.append(inputLine);
    }
    br.close();
    token = gson.fromJson(res.toString(), Token.class);

    return token;
  }
}
