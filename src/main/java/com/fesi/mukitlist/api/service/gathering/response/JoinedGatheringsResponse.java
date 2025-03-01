package com.fesi.mukitlist.api.service.gathering.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fesi.mukitlist.domain.gathering.GatheringType;
import com.fesi.mukitlist.domain.gathering.Keyword;
import com.fesi.mukitlist.domain.usergathering.UserGathering;

import lombok.Builder;

@Builder
public record JoinedGatheringsResponse(
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
	//image
	String createdBy,
	LocalDateTime canceledAt,
	LocalDateTime joinedAt,
	boolean isCompleted,
	boolean isReviewed
) {
	public static JoinedGatheringsResponse of(UserGathering userGathering, List<Keyword> keywords) {
		return JoinedGatheringsResponse.builder()
			.id(userGathering.getId().getGathering().getId())
			.type(userGathering.getId().getGathering().getType())
			.name(userGathering.getId().getGathering().getName())
			.dateTime(userGathering.getId().getGathering().getDateTime())
			.registrationEnd(userGathering.getId().getGathering().getRegistrationEnd())
			.location(userGathering.getId().getGathering().getLocation())
			.address1(userGathering.getId().getGathering().getAddress1())
			.address2(userGathering.getId().getGathering().getAddress2())
			.keywords(keywords.stream().map(Keyword::toString).collect(Collectors.toList()))
			.participantCount(userGathering.getId().getGathering().getParticipantCount())
			.capacity(userGathering.getId().getGathering().getCapacity())
			.createdBy(userGathering.getId().getGathering().getCreatedBy())
			.canceledAt(userGathering.getId().getGathering().getCanceledAt())
			.joinedAt(userGathering.getJoinedAt())
			// 모임이 완료됐다면 true를 반환 하는 로직 구현
			.isCompleted(true)
			// 로그인 한 유저가 리뷰를 남겼다면 true를 반환하는 로직 구현
			.isReviewed(true)
			.build();

	}
}
