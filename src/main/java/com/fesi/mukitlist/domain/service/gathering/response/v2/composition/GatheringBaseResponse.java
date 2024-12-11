package com.fesi.mukitlist.domain.service.gathering.response.v2.composition;

import java.time.LocalDateTime;

import com.fesi.mukitlist.domain.service.auth.response.UserResponse;
import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.gathering.constant.GatheringType;
import com.fesi.mukitlist.core.gathering.constant.LocationType;

public record GatheringBaseResponse(
	Long id,
	UserResponse user,
	GatheringType type,
	String name,
	LocalDateTime dateTime,
	LocalDateTime registrationEnd,
	LocationType location,
	String address1,
	String address2,
	int participantCount,
	int capacity,
	String image,
	LocalDateTime createdAt
) {
	public static GatheringBaseResponse of(Gathering gathering) {
		return new GatheringBaseResponse(
			gathering.getId(),
			UserResponse.of(gathering.getUser()),
			gathering.getType(),
			gathering.getName(),
			gathering.getDateTime(),
			gathering.getRegistrationEnd(),
			gathering.getLocation(),
			gathering.getAddress1(),
			gathering.getAddress2(),
			gathering.getParticipantCount(),
			gathering.getCapacity(),
			gathering.getUser().getImage(),
			gathering.getUser().getCreatedAt());
	}
}
