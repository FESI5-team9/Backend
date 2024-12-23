package com.fesi.mukitlist.domain.service.user;

import static org.assertj.core.api.Assertions.*;

import com.fesi.mukitlist.api.controller.auth.request.UserCreateRequest;
import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.repository.UserRepository;
import com.fesi.mukitlist.domain.service.auth.application.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

@ActiveProfiles("test")
@SpringBootTest
public class UserServiceTest {

    private String email;
    private String password ;
    private String nickname;
    private String anotherEmail;
    private String anotherNickname;
    private LocalDateTime CurrentDateTime = LocalDateTime.now();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        email = "user4@example.com";
        password = "test!123";
        nickname = "nickname4";
        anotherEmail = "user5@example.com";
        anotherNickname = "nickname5";

        // 닉네임 중복 체크 전 repository 데이터 삭제
        userRepository.deleteAll();

        User user = User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .createdAt(CurrentDateTime)
                .updatedAt(CurrentDateTime)
                .build();
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @DisplayName("유저 생성 시 중복된 이메일은 사용자 생성을 할 수 없습니다.")
    @Test
    void testRegisterUserWithExistEmail() {
        // given
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email(email)
                .password(password)
                .nickname(anotherNickname)
                .build();

        // when // then
        assertThatThrownBy(() -> userService.createUser(userCreateRequest.toServiceRequest()))
                .isInstanceOf(AppException.class)
                .extracting("exceptionCode")
                .extracting("code", "message")
                .containsExactly("EMAIL_EXIST", "중복된 이메일입니다.");
    }

    @DisplayName("유저 생성 시 중복된 닉네임은 사용자 생성을 할 수 없습니다.")
    @Test
    void testRegisterUserWithExistNickname() {
        // given
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email(anotherEmail)
                .password(password)
                .nickname(nickname)
                .build();

        // when // then
        assertThatThrownBy(() -> userService.createUser(userCreateRequest.toServiceRequest()))
                .isInstanceOf(AppException.class)
                .extracting("exceptionCode")
                .extracting("code", "message")
                .containsExactly("NICKNAME_EXIST", "중복된 닉네임입니다.");
    }

}
