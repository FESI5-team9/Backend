package com.fesi.mukitlist.domain.usergathering;

import java.time.LocalDateTime;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserGathering {

	@EmbeddedId
	private UserGatheringId id;

	private LocalDateTime joinedAt = LocalDateTime.now();

	private UserGathering(UserGatheringId id, LocalDateTime joinedAt) {
		this.id = id;
		this.joinedAt = joinedAt;
	}

	public static UserGathering of(UserGatheringId id, LocalDateTime joinedAt) {
		return new UserGathering(id, joinedAt);
	}
}
