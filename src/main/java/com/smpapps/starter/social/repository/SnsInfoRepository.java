package com.smpapps.starter.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.smpapps.starter.social.model.SnsInfo;

@RepositoryRestResource
public interface SnsInfoRepository extends JpaRepository<SnsInfo, Long> {

    Optional<SnsInfo> findByResponse_IdEquals(String id);
    
}
