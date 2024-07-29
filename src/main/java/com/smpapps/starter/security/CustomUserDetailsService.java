package com.smpapps.starter.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smpapps.starter.users.model.User;
import com.smpapps.starter.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.debug("Loading user by email: {}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> {
          log.debug("User not found with email: {}", email);
          return new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        });

    log.debug("User found: {}", user.getEmail());

    return new CustomUserDetails(user);
  }
}