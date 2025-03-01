package com.fesi.mukitlist.api.service.auth.request;

import com.fesi.mukitlist.domain.auth.TokenType;
import com.fesi.mukitlist.domain.auth.User;
import lombok.Builder;

@Builder
public record TokenServiceRequest(
        Long id,
        String token,
        TokenType tokenType,
        boolean expired,
        boolean revoked,
        User user
) {
    public TokenServiceRequest withUpdatedStatus(boolean expired, boolean revoked) {
        return new TokenServiceRequest(
                this.id,
                this.token,
                this.tokenType,
                expired,
                revoked,
                this.user
        );
    }
}
