package com.fesi.mukitlist.api.service.auth.request;

import lombok.Builder;

@Builder
public record AuthenticationServiceRequest(
        String email,
        String password
) {

}
