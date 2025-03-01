package com.fesi.mukitlist.api.service.review.request;

import java.time.LocalDateTime;

import com.fesi.mukitlist.domain.gathering.GatheringType;

import lombok.Builder;

@Builder
public record ReviewServiceRequest(
	Long gatheringId,
	Long userId,
	GatheringType type,
	String location,
	LocalDateTime date,
	LocalDateTime registrationEnd
) {

}
