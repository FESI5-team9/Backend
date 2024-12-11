package com.fesi.mukitlist.domain.service.gathering.response;

import java.time.LocalDateTime;

import com.fesi.mukitlist.core.usergathering.UserGathering;

import lombok.Builder;

@Builder
public record GatheringParticipantsResponse(
	Long gatheringId,
	LocalDateTime joinedAt,
	Long userId,
	String email,
	String nickname,
	String image
	) {
	public static GatheringParticipantsResponse of(UserGathering userGathering) {
		return GatheringParticipantsResponse.builder()
			.gatheringId(userGathering.getId().getGathering().getId())
			.joinedAt(userGathering.getJoinedAt())
			.userId(userGathering.getId().getUser().getId())
			.email(userGathering.getId().getUser().getEmail())
			.nickname(userGathering.getId().getUser().getNickname())
			.image(userGathering.getId().getUser().getImage())
			.build();
	}
}
