package com.fesi.mukitlist.api.controller.review.request;

import com.fesi.mukitlist.api.service.review.request.ReviewServiceCreateRequest;

public record ReviewCreateRequest(
	Long gatheringId,
	int score,
	String comment
) {

	public ReviewServiceCreateRequest toServiceRequest() {
		return ReviewServiceCreateRequest.builder()
			.gatheringId(gatheringId)
			.score(score)
			.comment(comment)
			.build();
	}
}
