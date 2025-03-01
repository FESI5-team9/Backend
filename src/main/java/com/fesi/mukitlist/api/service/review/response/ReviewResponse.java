package com.fesi.mukitlist.api.service.review.response;

import java.time.LocalDateTime;

import com.fesi.mukitlist.domain.Review;
import com.fesi.mukitlist.api.service.gathering.response.GatheringResponse;
import com.fesi.mukitlist.domain.auth.User;

import lombok.Builder;

@Builder
public record ReviewResponse(
	Long id,
	int score,
	String comment,
	LocalDateTime createdAt,
	GatheringResponse gathering,
	User user

)
{
	public static ReviewResponse of(Review review) {
		return ReviewResponse.builder()
			.id(review.getId())
			.score(review.getScore())
			.comment(review.getComment())
			.createdAt(review.getCreatedAt())
			.gathering(GatheringResponse.forReview(review.getGathering()))
			.user(review.getUser())
			.build();
	}
}
