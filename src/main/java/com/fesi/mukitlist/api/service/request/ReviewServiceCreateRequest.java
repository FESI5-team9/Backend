package com.fesi.mukitlist.api.service.request;

import com.fesi.mukitlist.api.controller.dto.request.ReviewCreateRequest;
import com.fesi.mukitlist.api.domain.Gathering;
import com.fesi.mukitlist.api.domain.Review;
import com.fesi.mukitlist.api.domain.User;

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