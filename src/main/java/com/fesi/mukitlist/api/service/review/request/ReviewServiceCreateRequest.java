package com.fesi.mukitlist.api.service.review.request;

import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.Gathering;
import com.fesi.mukitlist.domain.Review;

import lombok.Builder;

@Builder
public record ReviewServiceCreateRequest(
	Long gatheringId,
	int score,
	String comment
) {
	public ReviewServiceCreateRequest of(Review review) {
		return ReviewServiceCreateRequest.builder()
			.gatheringId(review.getGathering().getId())
			.score(review.getScore())
			.comment(review.getComment())
			.build();
	}

	public Review toEntity(Gathering gathering, User user) {
		return Review.of(
			score,
			comment,
			gathering,
			user
		);
	}
}