package com.fesi.mukitlist.domain.service.mypage;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fesi.mukitlist.api.controller.mypage.response.MyPageReviewResponse;
import com.fesi.mukitlist.api.controller.mypage.response.ReviewCompletedList;
import com.fesi.mukitlist.api.controller.mypage.response.ReviewUnCompletedList;
import com.fesi.mukitlist.core.Review;
import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.repository.gathering.GatheringRepository;
import com.fesi.mukitlist.core.repository.review.ReviewRepository;
import com.fesi.mukitlist.core.repository.usergathering.UserGatheringRepository;
import com.fesi.mukitlist.domain.service.gathering.response.GatheringListResponse;
import com.fesi.mukitlist.domain.service.review.response.ReviewResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class MyPageService {
	private final UserGatheringRepository userGatheringRepository;
	private final GatheringRepository gatheringRepository;
	private final ReviewRepository reviewRepository;

	public List<GatheringListResponse> getGatheringMypage(User user, Pageable pageable) {
		List<Gathering> response = gatheringRepository.findGatheringsByUser(user, pageable);
		return response.stream()
			.map(GatheringListResponse::of)
			.toList();
	}

	public MyPageReviewResponse getReviewMypage(User user) {
		List<Review> reviews = reviewRepository.findAllByUser(user);
		List<ReviewCompletedList> reviewCompletedLists = reviews.stream()
			.map(r -> ReviewCompletedList.of(GatheringListResponse.of(r.getGathering()), ReviewResponse.of(r)))
			.collect(Collectors.toList());

		List<Gathering> gatheringCandidates = userGatheringRepository.findGatheringsWithoutReviewsByUser(user);
		List<ReviewUnCompletedList> reviewUnCompletedLists = gatheringCandidates.stream()
			.map(g -> ReviewUnCompletedList.of(GatheringListResponse.of(g))).toList();

		return MyPageReviewResponse.of(reviewCompletedLists, reviewUnCompletedLists);

	}
}
