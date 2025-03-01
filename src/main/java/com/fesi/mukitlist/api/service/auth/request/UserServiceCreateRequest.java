package com.fesi.mukitlist.api.service.auth.request;

import lombok.Builder;

@Builder
public record UserServiceCreateRequest(
        String email,
        String password,
        String nickname,
        String name
        // String image
) {
}
