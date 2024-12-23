package com.fesi.mukitlist.api.controller.auth.request;

import com.fesi.mukitlist.domain.service.auth.request.UserServiceCreateRequest;
import com.fesi.mukitlist.core.auth.application.User;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO for creating a new {@link User}
 */
@Builder
public record UserCreateRequest(
	@NotEmpty(message = "이메일은 필수 입력값 입니다.")
	@Email(message = "유효한 이메일 형식이어야 합니다.")
	String email,
	@NotEmpty(message = "비밀번호는 필수 입력값 입니다.")
	@Pattern(
			regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\n",
			message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자(@$!%*#?&)를 모두 포함해야 합니다"
	)
	String password,
	@NotEmpty(message = "닉네임은 필수 입력값 입니다.")
	String nickname
	// String image
) {

	public UserServiceCreateRequest toServiceRequest() {
		return UserServiceCreateRequest.builder()
			.email(email)
			.password(password)
			.nickname(nickname)
			.build();
	}
}