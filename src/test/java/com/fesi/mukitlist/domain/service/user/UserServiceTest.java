package com.fesi.mukitlist.domain.service.user;

import static com.fesi.mukitlist.api.exception.ExceptionCode.EMAIL_EXIST;
import static com.fesi.mukitlist.api.exception.ExceptionCode.NICKNAME_EXIST;
import static org.assertj.core.api.Assertions.*;

import com.fesi.mukitlist.api.controller.auth.request.UserCreateRequest;
import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.repository.UserRepository;
import com.fesi.mukitlist.domain.service.auth.application.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

@ActiveProfiles("test")
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("유저 생성 시 중복된 이메일은 사용자 생성을 할 수 없습니다.")
    @Test
    void testRegisterUserWithExistEmail() {
        // given
        User user = createUser("test@test.com", "test");
        userRepository.save(user);

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email("test@test.com")
                .password("test1234!")
                .nickname("test2")
                .build();

        // when // then
        assertThatThrownBy(() -> userService.createUser(userCreateRequest.toServiceRequest()))
                .isInstanceOf(AppException.class)
                .extracting("exceptionCode")
                .isEqualTo(EMAIL_EXIST);
    }

    @DisplayName("유저 생성 시 중복된 닉네임은 사용자 생성을 할 수 없습니다.")
    @Test
    void testRegisterUserWithExistNickname() {
        // given
        User user = createUser("test@test.com", "test");
        userRepository.save(user);

        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .email("test1@test.com")
                .password("test1234!")
                .nickname("test")
                .build();

        // when // then
        assertThatThrownBy(() -> userService.createUser(userCreateRequest.toServiceRequest()))
                .isInstanceOf(AppException.class)
                .extracting("exceptionCode")
                .isEqualTo(NICKNAME_EXIST);
    }

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .password("test1234!")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
