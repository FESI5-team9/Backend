package com.fesi.mukitlist.api.service.gathering.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fesi.mukitlist.domain.gathering.GatheringType;

import lombok.Builder;

@Builder
public record GatheringServiceRequest(
	List<Long> id,
	GatheringType type,
	LocalDate dateTime,
	String location,
	String createdBy,
	int size,
	int page,
	String sort,
	String direction
) {

}
