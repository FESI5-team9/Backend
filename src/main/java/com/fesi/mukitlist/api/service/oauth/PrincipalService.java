package com.fesi.mukitlist.api.service.oauth;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.api.repository.UserRepository;
import com.fesi.mukitlist.domain.auth.PrincipalDetails;
import com.fesi.mukitlist.domain.auth.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrincipalService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(NOT_FOUND_USER));
        return new PrincipalDetails(user);
    }
}
