package com.fesi.mukitlist.api.controller.gathering.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fesi.mukitlist.domain.service.gathering.request.GatheringServiceUpdateRequest;

import io.swagger.v3.oas.annotations.media.Schema;

public record GatheringUpdateRequest(
	@Schema(description = "모임 이름", example = "런던 베이글 부수기")
	String name,

	@Schema(description = "최소 모집 인원 (자동 개설 확정)", example = "3")
	Integer openParticipantCount,

	@Schema(description = "모집 정원 (최소 5인 이상)", example = "10", minimum = "5")
	Integer capacity,

	@Schema(description = "모임 이미지", type = "string", format = "binary")
	MultipartFile image,

	String description,

	List<String> keyword
) {

	public GatheringServiceUpdateRequest toServiceRequest() {
		return new GatheringServiceUpdateRequest(
			name,
			openParticipantCount,
			capacity,
			image,
			description,
			keyword
		);
	}
}
