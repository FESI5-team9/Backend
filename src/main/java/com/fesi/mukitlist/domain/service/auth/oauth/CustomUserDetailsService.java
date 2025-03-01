package com.fesi.mukitlist.domain.service.auth.oauth;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.core.auth.PrincipalDetails;
import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username)
			.orElseThrow(() -> new AppException(NOT_FOUND_USER));
		return new PrincipalDetails(user);
	}
}
