package com.fesi.mukitlist.api.exception;

import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fesi.mukitlist.api.exception.response.AppErrorResponse;
import com.fesi.mukitlist.api.exception.response.ValidationErrorResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ApiControllerAdvice {

//	@ExceptionHandler(HttpMessageNotReadableException.class)
//	public ResponseEntity<ValidationErrorResponse> handle(HttpMessageNotReadableException e) {
//
//		String parameter = "";
//		if (e.getCause() instanceof JsonMappingException jsonMappingException) {
//			if (!jsonMappingException.getPath().isEmpty()) {
//				parameter = jsonMappingException.getPath().get(0).getFieldName();
//			}
//		}
//
//		assert parameter != null;
//		return new ResponseEntity<>(ValidationErrorResponse.of(
//			"VALIDATION_ERROR",
//			parameter,
//			responseMessage(parameter)
//		), HttpStatus.BAD_REQUEST);
//	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "400", description = "요청 오류",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = ValidationErrorResponse.class))),
		@ApiResponse(responseCode = "404", description = "조회 실패 오류",
			content = @Content(mediaType = "application/json",
				schema = @Schema(implementation = AppErrorResponse.class)))
	})
	public ResponseEntity<ValidationErrorResponse> handle(MethodArgumentNotValidException e) {
		BindingResult bindingResult = e.getBindingResult();
		FieldError fieldError = bindingResult.getFieldError();

		if (fieldError != null) {
			String parameter = fieldError.getField();
			String message = fieldError.getDefaultMessage();

			return new ResponseEntity<>(ValidationErrorResponse.of(
				"VALIDATION_ERROR",
				parameter,
				!"type".equals(parameter)
					? message
					: responseMessage(parameter)
			), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(ValidationErrorResponse.of(
			"VALIDATION_ERROR",
			"unknown",
			"유효하지 않은 요청입니다."
		), HttpStatus.BAD_REQUEST);

	}

//	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
//	@ApiResponses(value = {
//		@ApiResponse(responseCode = "400", description = "요청 오류",
//			content = @Content(mediaType = "application/json",
//				schema = @Schema(implementation = ValidationErrorResponse.class))),
//		@ApiResponse(responseCode = "404", description = "조회 실패 오류",
//			content = @Content(mediaType = "application/json",
//				schema = @Schema(implementation = AppErrorResponse.class)))
//	})
//	public ResponseEntity<ValidationErrorResponse> handle(MethodArgumentTypeMismatchException e) {
//
//		String parameterName = e.getName();
//
//		return new ResponseEntity<>(ValidationErrorResponse.of(
//			"VALIDATION_ERROR",
//			parameterName,
//			responseMessage(parameterName)
//		), HttpStatus.BAD_REQUEST);
//	}

	@ExceptionHandler(AppException.class)
	public ResponseEntity<AppErrorResponse> handle(AppException e) {
		return new ResponseEntity<>(AppErrorResponse.of(e.getExceptionCode()), e.getExceptionCode().getStatus());
	}

//	@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
//	protected ResponseEntity handleSQLException(SQLIntegrityConstraintViolationException e) {
//		log.error("ERROR: {}", e.getMessage(), e);
//		return new ResponseEntity(AppErrorResponse.of(ExceptionCode.EMAIL_EXIST), HttpStatus.INTERNAL_SERVER_ERROR);
//	}

	private String responseMessage(String parameter) {
		String message = "";
		if (parameter != null) {
			switch (parameter) {
				case "type" -> message = "유효한 모임 종류를 입력하세요";
				// 필요한 경우 추가 처리
			}
		}
		return message;
	}
}
