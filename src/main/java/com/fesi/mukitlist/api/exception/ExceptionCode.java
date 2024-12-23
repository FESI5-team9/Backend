package com.fesi.mukitlist.api.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ExceptionCode {
	//생성
	REQUIRED_PROPERTIES(HttpStatus.BAD_REQUEST, "REQUIRED_PROPERTIES", "필수 값이 입력되지 않았습니다."),

	//유저
	EMAIL_EXIST(HttpStatus.BAD_REQUEST, "EMAIL_EXIST", "중복된 이메일입니다."),
	NICKNAME_EXIST(HttpStatus.BAD_REQUEST, "NICKNAME_EXIST", "중복된 닉네임입니다."),
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, "NOT_FOUND", "사용자를 찾을 수 없습니다."),
	LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED", "로그인이 필요합니다."),

	//인증
	TOKEN_IS_NOT_IN_COOKIE(HttpStatus.BAD_REQUEST, "TOKEN_IS_NOT_IN_COOKIE", "토큰을 쿠키에서 찾을 수 없습니다."),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
	NOT_VALID_TOKEN(HttpStatus.UNAUTHORIZED, "NOT_VALID_TOKEN", "해당 토큰은 유효한 토큰이 아닙니다."),

	//모임
	AT_LEAST_ONE(HttpStatus.BAD_REQUEST, "AT_LEAST_ONE", "size는 최소 1이어야 합니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "모임을 찾을 수 없습니다."),
	MINIMUM_CAPACITY(HttpStatus.BAD_REQUEST, "MINIMUM_CAPACITY", "최소 인원은 5명입니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "모임을 변경할 권한이 없습니다."),
	PAST_GATHERING(HttpStatus.BAD_REQUEST, "PAST_GATHERING", "이미 지난 모임입니다."),

	//참여
	HOST_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "HOST_CANNOT_LEAVE", "호스트는 모임을 떠날 수 없습니다."),
	GATHERING_CANCELED(HttpStatus.BAD_REQUEST, "CANCELED_GATHERING", "취소된 모임입니다."),
	NOT_PARTICIPANTS(HttpStatus.FORBIDDEN, "FORBIDDEN", "모임에 참석하지 않았습니다."),
	MAXIMUM_PARTICIPANTS(HttpStatus.BAD_REQUEST, "MAXIMUM_PARTICIPANTS", "정원 초과 입니다."),
	ALREADY_JOINED_GATHERING(HttpStatus.BAD_REQUEST, "ALREADY_JOINED_GATHERING", "이미 참여한 모임입니다."),
	ALREADY_LEAVED_GATHERING(HttpStatus.BAD_REQUEST, "ALREADY_LEAVED_GATHERING", "이미 참여 취소한 모임입니다."),
	//파일
	RESOURCE_SIZE_LIMIT(HttpStatus.BAD_REQUEST, "SIZE_LIMIT", "파일의 용량은 10mb 이하여야 합니다."),

	//서버
	IO_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "IO_EXCEPTION", "입출력 오류가 발생했습니다."),
	SERVER_CHECK(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_CHECK", "서버 로그 확인을 요청해주세요.");

	private HttpStatus status;
	private String code;
	private String message;

	ExceptionCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
