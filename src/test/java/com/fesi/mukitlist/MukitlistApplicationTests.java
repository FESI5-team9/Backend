package com.fesi.mukitlist;

import com.fesi.mukitlist.api.controller.auth.request.UserCreateRequest;
import com.fesi.mukitlist.api.controller.auth.response.UserCreateResponse;
import com.fesi.mukitlist.domain.service.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)  // 실제 서버 환경에서 테스트
class MukitlistApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;  // HTTP 요청을 보내기 위한 템플릿

	@Autowired
	private JwtService jwtService;  // JwtService를 자동 주입

	private UserDetails userDetails;

	@Test
	void testRegisterUser() {
		// given
		UserCreateRequest userCreateRequest = UserCreateRequest.builder()
				.email("test@test.com")
				.password("password123")
				.nickname("테스트")
				.build();

		// when
		ResponseEntity<UserCreateResponse> response = restTemplate.postForEntity(
				"/api/auth/signup",  // 사용자 등록 API 엔드포인트
				userCreateRequest,
				UserCreateResponse.class  // 응답은 사용자 등록을 위한 적절한 응답 객체로 설정
		);

		// then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);  // 등록된 이메일 중복 INTERNAL_SERVER_ERROR
		assertThat(response.getBody()).isNotNull();  // 응답 바디가 null이 아님
	}
}
