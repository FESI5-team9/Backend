package com.fesi.mukitlist.api.controller.dto.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(String token) {

}
