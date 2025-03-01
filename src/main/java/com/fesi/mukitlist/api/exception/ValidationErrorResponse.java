package com.fesi.mukitlist.api.exception;

import lombok.Getter;

@Getter
public class ValidationErrorResponse {
	private String code;
	private String parameter;
	private String message;

	private ValidationErrorResponse(String code, String parameter, String message) {
		this.code = code;
		this.parameter = parameter;
		this.message = message;
	}

	public static ValidationErrorResponse of(String code, String parameter, String message) {
		return new ValidationErrorResponse(code, parameter, message);
	}
}
