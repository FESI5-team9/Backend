package com.fesi.mukitlist.api.service.gathering.request;

import java.time.LocalDate;
import java.util.List;

import com.fesi.mukitlist.domain.gathering.constant.GatheringType;
import com.fesi.mukitlist.domain.gathering.constant.LocationType;

import lombok.Builder;

@Builder
public record GatheringServiceRequest(
	List<Long> id,
	GatheringType type,
	LocalDate dateTime,
	LocationType location,
	String createdBy,
	int size,
	int page,
	String sort,
	String direction
) {

}
