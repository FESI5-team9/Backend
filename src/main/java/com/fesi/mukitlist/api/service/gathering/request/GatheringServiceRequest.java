package com.fesi.mukitlist.api.service.gathering.request;

import java.time.LocalDateTime;
import java.util.List;

import com.fesi.mukitlist.domain.gathering.GatheringType;

import lombok.Builder;

@Builder
public record GatheringServiceRequest(
	List<Long> id,
	GatheringType type,
	LocalDateTime dateTime,
	String location,
	String createdBy
) {

}
