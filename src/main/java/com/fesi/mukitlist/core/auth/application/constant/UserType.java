package com.fesi.mukitlist.core.auth.application.constant;

import lombok.Getter;

@Getter
public enum UserType {
    KAKAO("KAKAO"),
    GOOGLE("GOOGLE"),
    NORMAL("DEFAULT");

    private final String providerName;
    UserType(String providerName) {
        this.providerName = providerName;
    }
}
