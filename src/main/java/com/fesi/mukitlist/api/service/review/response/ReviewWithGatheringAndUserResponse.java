package com.fesi.mukitlist.api.service.review.response;

import java.time.LocalDateTime;

import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.Gathering;
import com.fesi.mukitlist.domain.Review;

import lombok.Builder;

@Builder
public record ReviewWithGatheringAndUserResponse(
	Long id,
	int score,
	String comment,
	LocalDateTime createdAt,
	Gathering gathering,
	User user
	)
{
	public static ReviewWithGatheringAndUserResponse of(Review review) {
		return ReviewWithGatheringAndUserResponse.builder()
			.id(review.getId())
			.score(review.getScore())
			.comment(review.getComment())
			.createdAt(review.getCreatedAt())
			.user(review.getUser())
			.gathering(review.getGathering())
			.build();
	}
}
