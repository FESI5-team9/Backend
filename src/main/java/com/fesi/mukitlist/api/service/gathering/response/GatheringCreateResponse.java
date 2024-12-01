package com.fesi.mukitlist.api.service.gathering.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fesi.mukitlist.domain.gathering.Gathering;
import com.fesi.mukitlist.domain.gathering.GatheringType;
import com.fesi.mukitlist.domain.gathering.Keyword;

public record GatheringCreateResponse(
	Long id,
	GatheringType type,
	String name,
	LocalDateTime dateTime,
	LocalDateTime registrationEnd,
	String location,
	String address1,
	String address2,
	List<String> keywords,
	int participantCount,
	int capacity,
	String image,
	String createdBy
) {
	public static GatheringCreateResponse of(Gathering gathering, List<Keyword> keywords) {
		return new GatheringCreateResponse(
			gathering.getId(),
			gathering.getType(),
			gathering.getName(),
			gathering.getDateTime(),
			gathering.getRegistrationEnd(),
			gathering.getLocation(),
			gathering.getAddress1(),
			gathering.getAddress2(),
			keywords.stream().map(Keyword::toString).toList(),
			gathering.getParticipantCount(),
			gathering.getCapacity(),
			gathering.getImage(),
			gathering.getCreatedBy()
		);
	}
}
