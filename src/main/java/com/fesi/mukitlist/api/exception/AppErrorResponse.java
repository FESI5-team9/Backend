package com.fesi.mukitlist.api.exception;

import lombok.Getter;

@Getter
public class AppErrorResponse {
	private String code;
	private String message;

	private AppErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public static AppErrorResponse of(ExceptionCode e) {
		return new AppErrorResponse(e.getCode(), e.getMessage());
	}
}
