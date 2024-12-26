package com.fesi.mukitlist.domain.service.gathering.response;

import java.time.LocalDateTime;

import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.gathering.constant.GatheringStatus;
import com.fesi.mukitlist.core.gathering.constant.GatheringType;
import com.fesi.mukitlist.core.gathering.constant.LocationType;

import lombok.Getter;

@Getter
public class GatheringListResponse {
	private Long id;
	private GatheringStatus status;
	private GatheringType type;
	private String name;
	private LocalDateTime dateTime;
	private LocalDateTime registrationEnd;
	private LocationType location;
	private String address1;
	private String address2;
	private Boolean open;
	private boolean favorite;
	private boolean participation;
	private int participantCount;
	private int capacity;
	private String image;
	private LocalDateTime createdAt;
	private LocalDateTime canceledAt;

	private GatheringListResponse(Long id, GatheringStatus status, GatheringType type, String name, LocalDateTime dateTime,
		LocalDateTime registrationEnd, LocationType location, String address1, String address2, Boolean open, boolean favorite,
		boolean participation, int participantCount, int capacity, String image, LocalDateTime createdAt, LocalDateTime canceledAt) {
		this.id = id;
		this.status = status;
		this.type = type;
		this.name = name;
		this.dateTime = dateTime;
		this.registrationEnd = registrationEnd;
		this.location = location;
		this.address1 = address1;
		this.address2 = address2;
		this.open = open;
		this.favorite = favorite;
		this.participation = participation;
		this.participantCount = participantCount;
		this.capacity = capacity;
		this.image = image;
		this.createdAt = createdAt;
		this.canceledAt = canceledAt;
	}

	public static GatheringListResponse of(Gathering gathering) {
		return new GatheringListResponse(
			gathering.getId(),
			gathering.getStatus(),
			gathering.getType(),
			gathering.getName(),
			gathering.getDateTime(),
			gathering.getRegistrationEnd(),
			gathering.getLocation(),
			gathering.getAddress1(),
			gathering.getAddress2(),
			gathering.isOpenedGathering(),
			false,
			false,
			gathering.getOpenParticipantCount(),
			gathering.getCapacity(),
			gathering.getImage(),
			gathering.getCreatedAt(),
			gathering.getCanceledAt()
		);
	}

	public void updateParticipationStatusTrue() {
		this.participation = true;
	}

	public void updateFavoriteStatusTrue() {
		this.favorite = true;
	}
}
