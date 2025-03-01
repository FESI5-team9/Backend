package com.fesi.mukitlist.api.service.gathering.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fesi.mukitlist.domain.gathering.Gathering;
import com.fesi.mukitlist.domain.gathering.GatheringType;
import com.fesi.mukitlist.domain.gathering.Keyword;

import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record GatheringResponse(
	Long id,
	GatheringType type,
	String name,
	LocalDateTime dateTime,
	LocalDateTime registrationEnd,
	String location,
	String address1,
	String address2,
	String description,
	List<String> keyword,
	int participantCount,
	int capacity,
	String image,
	String createdBy,
	@JsonInclude(JsonInclude.Include.ALWAYS)
	LocalDateTime canceledAt
) {

	public static GatheringResponse forList(Gathering gathering, List<Keyword> keywords) {
		return GatheringResponse.builder()
			.id(gathering.getId())
			.type(gathering.getType())
			.name(gathering.getName())
			.dateTime(gathering.getDateTime())
			.registrationEnd(gathering.getRegistrationEnd())
			.location(gathering.getLocation())
			.address1(gathering.getAddress1())
			.keyword(keywords.stream().map(Keyword::toString).collect(Collectors.toList()))
			.participantCount(gathering.getParticipantCount())
			.capacity(gathering.getCapacity())
			.image(gathering.getUser().getImage())
			.createdBy(gathering.getUser().getName())
			.canceledAt(gathering.getCanceledAt())
			.build();
	}

	public static GatheringResponse forDetail(Gathering gathering, List<Keyword> keywords) {
		return GatheringResponse.builder()
			.id(gathering.getId())
			.type(gathering.getType())
			.name(gathering.getName())
			.dateTime(gathering.getDateTime())
			.registrationEnd(gathering.getRegistrationEnd())
			.location(gathering.getLocation())
			.address1(gathering.getAddress1())
			.address2(gathering.getAddress2())
			.description(gathering.getDescription())
			.keyword(keywords.stream().map(Keyword::toString).collect(Collectors.toList()))
			.participantCount(gathering.getParticipantCount())
			.capacity(gathering.getCapacity())
			.image(gathering.getUser().getImage())
			.createdBy(gathering.getUser().getName())
			.canceledAt(gathering.getCanceledAt())
			.build();
	}

	public static GatheringResponse forReview(Gathering gathering) {
		return GatheringResponse.builder()
			.id(gathering.getId())
			.type(gathering.getType())
			.name(gathering.getName())
			.dateTime(gathering.getDateTime())
			.location(gathering.getLocation())
			.image(gathering.getUser().getImage())
			.build();
	}
}
