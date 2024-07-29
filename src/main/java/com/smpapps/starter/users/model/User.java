package com.smpapps.starter.users.model;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @JsonIgnore
  @Column(nullable = false)
  private String password;

  // @Column(nullable = false)
  // private String name;
  
  @Column(nullable = false)
  private String authority; // 권한 ADMIN, COMPANY, NORMAL, ANONYMOUS

  @JsonIgnore
  @Column(columnDefinition = "varchar(255) default 'RULE_NORMAL'")
  @Transient
  public Collection<? extends GrantedAuthority> getUserAuthorities() {
    ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    authorities.add(new SimpleGrantedAuthority(authority));
    return authorities;
  }

  @Builder.Default
  private boolean accountNonExpired = true;

  @Builder.Default
  private boolean accountNonLocked = true;

  @Builder.Default
  private boolean credentialsNonExpired = true;

  @Builder.Default
  private boolean enabled = true;

  // password에 대한 getter는 제거하고, setter만 유지
  @JsonIgnore
  public void setPassword(String password) {
    this.password = password;
  }
}