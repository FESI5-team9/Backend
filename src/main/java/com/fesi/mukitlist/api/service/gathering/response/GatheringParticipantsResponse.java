package com.fesi.mukitlist.api.service.gathering.response;

import java.time.LocalDateTime;

import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.usergathering.UserGathering;

import lombok.Builder;

@Builder
public record GatheringParticipantsResponse(
	Long userId,
	Long gatheringId,
	LocalDateTime joinedAt,
	User user
) {
	public static GatheringParticipantsResponse of(UserGathering userGathering) {
		return GatheringParticipantsResponse.builder()
			.userId(userGathering.getId().getUser().getId())
			.gatheringId(userGathering.getId().getGathering().getId())
			.joinedAt(userGathering.getJoinedAt())
			.user(userGathering.getId().getUser())
			.build();
	}
}
