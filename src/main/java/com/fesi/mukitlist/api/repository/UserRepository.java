package com.fesi.mukitlist.api.repository;

import com.fesi.mukitlist.core.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);

	boolean existsUserByEmail(String email);

	boolean existsUserByNickname(String nickname);
}

