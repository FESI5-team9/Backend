package com.fesi.mukitlist.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fesi.mukitlist.core.auth.Token;
import com.fesi.mukitlist.core.auth.User;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query(value = """
      select t from Token t inner join User u
      on t.user.id = u.id
      where u.id = :userId and t.token = :refreshToken and t.expired = false
      """)
    Token findByUserAndToken(@Param("userId") Long userId, @Param("refreshToken") String refreshToken);

    boolean existsTokenByUserIdAndToken(Long userI, String refreshToken);
	Optional<Token> findByUser(User user);

    Token findByToken(String token);

}