package com.fesi.mukitlist.api.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fesi.mukitlist.api.service.auth.request.TokenServiceRequest;
import com.fesi.mukitlist.api.service.auth.response.AuthenticationServiceResponse;
import com.fesi.mukitlist.domain.auth.Token;
import com.fesi.mukitlist.domain.auth.TokenType;
import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.api.repository.TokenRepository;
import com.fesi.mukitlist.api.repository.UserRepository;
import com.fesi.mukitlist.api.service.auth.request.AuthenticationServiceRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceResponse authenticate(AuthenticationServiceRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        User user = repository.findByEmail(request.email())
                .orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationServiceResponse.builder()
                .accessToken(jwtToken)
                    .refreshToken(jwtToken)
                .build();
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        
        if (validUserTokens.isEmpty()) {
            return;
        }

        List<TokenServiceRequest> updatedTokens = validUserTokens.stream()
                .map(token -> new TokenServiceRequest(
                        token.getId(),             
                        token.getToken(),          
                        token.getTokenType(),      
                        true,                
                        true,               
                        token.getUser()          
                ).withUpdatedStatus(true, true)) 
                .collect(Collectors.toList());

        List<Token> tokensToSave = updatedTokens.stream()
                .map(this::convertToTokenEntity)
                .collect(Collectors.toList());

        tokenRepository.saveAll(tokensToSave);
    }

    private Token convertToTokenEntity(TokenServiceRequest request) {
        return new Token(
                request.id(),
                request.token(),
                request.tokenType(),
                request.expired(),
                request.revoked(),
                request.user()
        );
    }

    private void saveUserToken(User user, String refreshToken) {
        var token = Token.builder()
                .user(user)
                .token(refreshToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);

        log.info("Refresh Token saved:: {}", token.getToken());
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, refreshToken);
                var authResponse = AuthenticationServiceResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}