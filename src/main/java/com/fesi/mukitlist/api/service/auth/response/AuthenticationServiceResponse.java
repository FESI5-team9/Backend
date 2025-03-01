package com.fesi.mukitlist.api.service.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthenticationServiceResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken
) {
    public static AuthenticationServiceResponse of(String accessToken, String refreshToken) {
        return new AuthenticationServiceResponse(accessToken, refreshToken);
    }
}
