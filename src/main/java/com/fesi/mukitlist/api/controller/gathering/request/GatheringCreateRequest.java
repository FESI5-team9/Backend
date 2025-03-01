package com.fesi.mukitlist.api.controller.gathering.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fesi.mukitlist.domain.gathering.GatheringType;
import com.fesi.mukitlist.api.service.gathering.request.GatheringServiceCreateRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GatheringCreateRequest(
	@Schema(description = "모임 서비스 종류", example = "식당,카페,주점,비건")
	GatheringType type,

	@Schema(description = "모임 장소", example = "서울 강남구")
	@NotBlank(message = "모임 장소를 입력해주세요.")
	String location,

	@Schema(description = "모임 이름", example = "맛집 탐방")
	@NotBlank(message = "모임 이름을 입력해주세요.")
	String name,

	@Schema(description = "모임 날짜 및 시간 (YYYY-MM-DDTHH:MM:SS)", example = "2024-12-01T18:00:00", type = "string", format = "date-time")
	LocalDateTime dateTime,

	@Schema(description = "모집 정원 (최소 5인 이상)", example = "10", minimum = "5")
	int capacity,

	// MultipartFile image
	@Schema(description = "모임 등록 마감일 (YYYY-MM-DDTHH:MM:SS)", example = "2024-11-30T23:59:59", type = "string", format = "date-time")
	LocalDateTime registrationEnd,
	String address1,
	String address2,
	String description,
	List<String> keyword
) {

	public GatheringServiceCreateRequest toServiceRequest() {
		return GatheringServiceCreateRequest.builder()
			.location(location)
			.type(type)
			.name(name)
			.dateTime(dateTime)
			.capacity(capacity)
			.registrationEnd(registrationEnd)
			.address1(address1)
			.address2(address2)
			.description(description)
			.keyword(keyword)
			.build();
	}
}
