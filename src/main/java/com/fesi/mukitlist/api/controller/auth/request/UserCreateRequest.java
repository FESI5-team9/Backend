package com.fesi.mukitlist.api.controller.auth.request;

import com.fesi.mukitlist.domain.service.auth.request.UserServiceCreateRequest;
import com.fesi.mukitlist.core.auth.application.User;

import lombok.Builder;

/**
 * DTO for creating a new {@link User}
 */
@Builder
public record UserCreateRequest(
	String email,
	String password,
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