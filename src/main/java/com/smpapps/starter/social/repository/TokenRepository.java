package com.smpapps.starter.social.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smpapps.starter.social.model.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
    
}
