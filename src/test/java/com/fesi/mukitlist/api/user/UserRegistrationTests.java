package com.fesi.mukitlist.api.user;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import com.fesi.mukitlist.core.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fesi.mukitlist.api.controller.auth.request.UserCreateRequest;
import com.fesi.mukitlist.api.controller.auth.response.UserCreateResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRegistrationTests {

	private String email;
	private String password;
	private String nickname;

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		email = "test@test.com";
		password = "test1234!";
		nickname = "test";
	}

	@DisplayName("유저를 생성합니다.")
	@Test
	void testRegisterUser() {
		// given
		UserCreateRequest userCreateRequest = UserCreateRequest.builder()
			.email(email)
			.password(password)
			.nickname(nickname)
			.build();

		// when
		ResponseEntity<UserCreateResponse> response = restTemplate.postForEntity(
			"/api/auth/signup",
			userCreateRequest,
			UserCreateResponse.class
		);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
	}

	@DisplayName("유저 생성 시 이메일은 필수로 입력해야 합니다.")
	@Test
	void testRegisterUserWithEmailEmpty() {
		// given
		UserCreateRequest invalidRequest = UserCreateRequest.builder()
				.email("")
				.password(password)
				.nickname(nickname)
				.build();

		// when
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/auth/signup",
				invalidRequest,
				String.class
		);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("이메일은 필수 입력값 입니다.");
	}

	@DisplayName("유저 생성 시 유요한 이메일 형식으로 작성해야 합니다.")
	@Test
	void testRegisterUserWithInvalidEmail() {
		// given
		UserCreateRequest userCreateRequest = UserCreateRequest.builder()
				.email("test")
				.password(password)
				.nickname(nickname)
				.build();

		// when
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/auth/signup",
				userCreateRequest,
				String.class
		);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("유효한 이메일 형식이어야 합니다.");
	}

	@DisplayName("유저 생성 시 비밀번호는 규칙에 맞아야 합니다.")
	@Test
	void testRegisterUserWithInvalidPassword() {
		// given
		UserCreateRequest invalidRequest = UserCreateRequest.builder()
				.email(email)
				.password("test")
				.nickname(nickname)
				.build();

		// when
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/auth/signup",
				invalidRequest,
				String.class
		);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("비밀번호는 8자 이상, 영문, 숫자, 특수문자(@$!%*#?&)를 모두 포함해야 합니다");
	}

	@DisplayName("유저 생성 시 닉네임은 필수로 입력해야 합니다.")
	@Test
	void testRegisterUserWithNickcnameEmpty() {
		// given
		UserCreateRequest invalidRequest = UserCreateRequest.builder()
				.email(email)
				.password(password)
				.nickname("")
				.build();

		// when
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/auth/signup",
				invalidRequest,
				String.class
		);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("닉네임은 필수 입력값 입니다.");
	}

}