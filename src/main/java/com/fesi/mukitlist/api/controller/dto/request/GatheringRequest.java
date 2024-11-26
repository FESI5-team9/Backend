package com.fesi.mukitlist.api.controller.dto.request;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fesi.mukitlist.api.domain.GatheringType;
import com.fesi.mukitlist.api.service.request.GatheringServiceRequest;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * DTO for {@link com.fesi.mukitlist.api.domain.Gathering}
 */
public record GatheringRequest(

	List<Long> id,
	GatheringType type,
	LocalDateTime dateTime,
	String location,
	String createdBy
)
	implements Serializable {

	public static GatheringRequest of(List<Long> id, GatheringType type, LocalDateTime dateTime, String location,
		String createdBy) {
		return new GatheringRequest(id, type, dateTime, location, createdBy);
	}

	@Builder
	public GatheringServiceRequest toServiceRequest() {
		return GatheringServiceRequest.builder()
			.id(id)
			.type(type)
			.dateTime(dateTime)
			.location(location)
			.createdBy(createdBy)
			.build();
	}
}