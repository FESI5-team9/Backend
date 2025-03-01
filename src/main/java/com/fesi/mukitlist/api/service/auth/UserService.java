package com.fesi.mukitlist.api.service.auth;

import com.fesi.mukitlist.api.service.auth.request.UserServiceCreateRequest;
import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserServiceCreateRequest request) {
        // 이메일 UK 걸려있으면
        // Optional<User> existed = userRepository.findByEmail(email);
        // if(existed.isPresent()){
        //     throw new EmailExistedException(email);
        // }

        String encodePassword = passwordEncoder.encode(request.password()); // 해싱하는 부분
        User user = User.of(request, encodePassword);
        return userRepository.save(user);
    }

}