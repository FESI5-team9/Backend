package com.fesi.mukitlist.domain.service.auth;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;
import static com.fesi.mukitlist.core.auth.GrantType.*;
import static com.fesi.mukitlist.core.auth.TokenType.*;

import java.io.IOException;

import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fesi.mukitlist.api.controller.auth.response.AuthenticationResponse;
import com.fesi.mukitlist.api.controller.auth.response.AuthenticationResponseV2;
import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.core.auth.PrincipalDetails;
import com.fesi.mukitlist.core.auth.Token;
import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.repository.TokenRepository;
import com.fesi.mukitlist.core.repository.UserRepository;
import com.fesi.mukitlist.domain.service.auth.request.AuthenticationServiceRequest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationService {
	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;

	public String checkRefreshToken(PrincipalDetails principalDetails) {
		if (tokenIsEmpty(principalDetails)) {
			String token = jwtService.generateRefreshToken(principalDetails);
			Token savedToken = Token.builder()
				.user(principalDetails.getUser())
				.token(token)
				.grantType(BEARER)
				.tokenType(REFRESH)
				.expired(false)
				.build();
			return tokenRepository.save(savedToken).getToken();
		} else {
			return tokenRepository.findFirstByUserId(principalDetails.getUser().getId()).getToken();
		}
	}

	public AuthenticationResponse authenticate(AuthenticationServiceRequest request, HttpServletResponse response) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new AppException(NOT_FOUND_USER));
		PrincipalDetails principalDetails = new PrincipalDetails(user);

		String accessToken = jwtService.generateAccessToken(principalDetails);
		String refreshToken = checkRefreshToken(principalDetails);

		ResponseCookie cookie = addRefreshTokenToCookie(refreshToken);
		response.addHeader("Set-Cookie", cookie.toString());

		return AuthenticationResponse.builder()
			.accessToken(accessToken)
			.build();
	}

	public AuthenticationResponseV2 authenticate(AuthenticationServiceRequest request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new AppException(NOT_FOUND_USER));

		validatePassword(request.password(), user);

		PrincipalDetails principalDetails = new PrincipalDetails(user);

		String accessToken = jwtService.generateAccessToken(principalDetails);
		String refreshToken = checkRefreshToken(principalDetails);

		return AuthenticationResponseV2.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	private void validatePassword(String password, User user) {
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new AppException(WRONG_PASSWORD);
		}
	}

	public String generateToNewAccessToken(String refreshToken) throws IOException {
		Token refreshTokenEntity = tokenRepository.findFirstByToken(refreshToken);
		if (refreshTokenEntity != null) {
			return jwtService.generateAccessToken(new PrincipalDetails(refreshTokenEntity.getUser()));
		}
		throw new AppException(TOKEN_IS_NOT_IN_COOKIE);
	}

	public ResponseCookie addRefreshTokenToCookie(String refreshToken) {
		return ResponseCookie.from("refresh-token", refreshToken)
			.httpOnly(true)
			.secure(false)
			.path("/")
			.sameSite("None")
			.maxAge(60 * 60 * 24 * 7)
			.build();
	}

	private boolean tokenIsEmpty(PrincipalDetails principalDetails) {
		return !tokenRepository.existsTokenByUserId(principalDetails.getUser().getId());
	}
}