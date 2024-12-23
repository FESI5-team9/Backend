package com.fesi.mukitlist.api.controller.auth;

import java.io.IOException;
import java.util.Map;

import com.fesi.mukitlist.api.exception.response.ValidationErrorResponse;
import com.fesi.mukitlist.core.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fesi.mukitlist.api.controller.auth.request.ManagedTokenRequest;
import com.fesi.mukitlist.api.controller.auth.request.UserCreateRequest;
import com.fesi.mukitlist.api.controller.auth.response.AuthenticationResponse;
import com.fesi.mukitlist.api.controller.auth.response.AuthenticationResponseV2;
import com.fesi.mukitlist.api.response.SimpleApiResponse;
import com.fesi.mukitlist.domain.service.auth.AuthenticationService;
import com.fesi.mukitlist.domain.service.auth.application.UserService;
import com.fesi.mukitlist.domain.service.auth.request.AuthenticationServiceRequest;
import com.fesi.mukitlist.domain.service.auth.response.UserInfoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthenticationController {

	private final AuthenticationService authenticationService;
	private final UserService userService;

	@Value("${kakao.client.id}")
	private String clientId;

	@Value("${kakao.client.redirect_url}")
	private String redirectUri;

	@Operation(summary = "회원가입", description = "회원 가입을 진행합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "회원 가입 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = UserInfoResponse.class))),
			@ApiResponse(
				responseCode = "400",
				description = "타입 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"VALIDATION_ERROR\", \"parameter\":\"email\", \"message\":\"이메일 양식을 지켜주세요.\"}"
					)
				)
			),
			@ApiResponse(
				responseCode = "400",
				description = "요청 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = ValidationErrorResponse.class)))
		}
	)
	@PostMapping("/signup")
	public ResponseEntity<SimpleApiResponse> signup(
		@Valid @RequestBody UserCreateRequest userCreateRequest) {
		userService.createUser(userCreateRequest.toServiceRequest());
		return new ResponseEntity<>(SimpleApiResponse.of("사용자 생성 성공"), HttpStatus.CREATED);
	}

//	@Operation(summary = "로그인", description = "로그인을 시도합니다.",
//		responses = {
//			@ApiResponse(responseCode = "200", description = "로그인 성공",
//				content = @Content(
//					mediaType = "application/json",
//					schema = @Schema(implementation = AuthenticationResponse.class))),
//			@ApiResponse(
//				responseCode = "403",
//				description = "권한 오류",
//				content = @Content(
//					mediaType = "application/json",
//					schema = @Schema(
//						example = "{\"code\":\"FORBIDDEN\",\"message\":\"권한이 없습니다.\"}"
//					)
//				)
//			),
//			@ApiResponse(
//				responseCode = "404",
//				description = "유저 없음",
//				content = @Content(
//					mediaType = "application/json",
//					schema = @Schema(
//						example = "{\"code\":\"NOT_FOUND\",\"message\":\"유저를 찾을 수 없습니다.\"}"
//					)
//				)
//			),
//		}
//	)
//	@PostMapping("/signin")
//	public ResponseEntity<AuthenticationResponseV2> signInV2(
//			@RequestBody KakaoLoginRequest request) {
//		return ResponseEntity.ok(authenticationService.authenticate(request));
//	}

	@Operation(summary = "로그인", description = "로그인을 시도합니다.",
			responses = {
					@ApiResponse(responseCode = "200", description = "로그인 성공",
							content = @Content(
									mediaType = "application/json",
									schema = @Schema(implementation = AuthenticationResponseV2.class)
							))
			}
	)
	@PostMapping("/v2/signin")
	public ResponseEntity<AuthenticationResponseV2> signInV2(
			@RequestBody AuthenticationServiceRequest request) {
		return ResponseEntity.ok(authenticationService.authenticate(request));
	}

//	@PostMapping("kakao/signin")
//	public ResponseEntity<AuthenticationResponseV2> signInKakao(
//			@RequestBody KakaoLoginRequest request) {
//		return ResponseEntity.ok(authenticationService.authenticate(request));
//	}

	@Operation(summary = "이메일 중복확인", description = "이메일 중복 확인을 진행합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "중복 확인 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = SimpleApiResponse.class))),
			@ApiResponse(
				responseCode = "400",
				description = "타입 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"VALIDATION_ERROR\", \"parameter\":\"email\", \"message\":\"이메일 양식을 지켜주세요.\"}"
					)
				)
			),
		}
	)
	@GetMapping("/check-email")
	public ResponseEntity<Map<String, Boolean>> checkEmailDuplicated(@RequestParam String email) {
		return new ResponseEntity(SimpleApiResponse.of(String.valueOf(userService.checkEmail(email))), HttpStatus.OK);
	}

	@Operation(summary = "닉네임 중복확인", description = "닉네임 중복 확인을 진행합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "중복 확인 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = SimpleApiResponse.class))),
		}
	)
	@GetMapping("/check-nickname")
	public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicated(@RequestParam String nickname) {
		return new ResponseEntity(SimpleApiResponse.of(String.valueOf(userService.checkNickname(nickname))),
			HttpStatus.OK);
	}

	@Operation(
		summary = "새로운 액세스 토큰 발급",
		description = "쿠키에서 리프레시 토큰을 가져와 유효성을 검사한 후 새로운 액세스 토큰을 발급합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PostMapping("/managed-access-token") // TODO 더 좋게 받을 방법 있을까 고민
	public ResponseEntity<AuthenticationResponse> refreshToken(
		@Parameter(hidden = true) @RequestHeader(value = "Authorization") String header) throws IOException {
		String refreshToken = header.split(" ")[1];
		return ResponseEntity.ok(
			AuthenticationResponse.of(authenticationService.generateToNewAccessToken(refreshToken)));
	}

	@Operation(
		summary = "새로운 액세스 토큰 발급 v2",
		description = "쿠키에서 리프레시 토큰을 가져와 유효성을 검사한 후 새로운 액세스 토큰을 발급합니다.",
		security = @SecurityRequirement(name = "bearerAuth")
	)
	@PostMapping("/v2/managed-access-token")
	public ResponseEntity<AuthenticationResponse> refreshTokenV2(
		@RequestBody ManagedTokenRequest request) throws IOException {
		return ResponseEntity.ok(
			AuthenticationResponse.of(authenticationService.generateToNewAccessToken(request.refreshToken())));
	}

}
