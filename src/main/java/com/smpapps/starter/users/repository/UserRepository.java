package com.smpapps.starter.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.smpapps.starter.users.model.User;

@RepositoryRestResource
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndJoinChannel(String email, String joinChannel);
}
