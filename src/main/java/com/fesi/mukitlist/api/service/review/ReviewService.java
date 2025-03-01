package com.fesi.mukitlist.api.service.review;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.api.repository.ReviewRepository;
import com.fesi.mukitlist.api.repository.usergathering.UserGatheringRepository;
import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.Gathering;
import com.fesi.mukitlist.domain.gathering.GatheringType;
import com.fesi.mukitlist.domain.Review;
import com.fesi.mukitlist.api.repository.GatheringRepository;
import com.fesi.mukitlist.api.repository.UserRepository;
import com.fesi.mukitlist.api.service.review.request.ReviewServiceCreateRequest;
import com.fesi.mukitlist.api.service.review.request.ReviewServiceRequest;
import com.fesi.mukitlist.api.service.review.response.ReviewResponse;
import com.fesi.mukitlist.api.service.review.response.ReviewScoreResponse;
import com.fesi.mukitlist.api.service.review.response.ReviewWithGatheringAndUserResponse;
import com.fesi.mukitlist.domain.usergathering.UserGatheringId;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final GatheringRepository gatheringRepository;
	private final UserRepository userRepository;
	private final UserGatheringRepository userGatheringRepository;

	@Transactional(readOnly = true)
	public List<ReviewWithGatheringAndUserResponse> getReviews(ReviewServiceRequest request, Pageable pageable) {

		Specification<Review> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			// Gathering 조인
			Join<Review, Gathering> gatheringJoin = root.join("gathering", JoinType.INNER);

			// User 조인
			Join<Review, User> userJoin = root.join("user", JoinType.INNER);

			// 필터 조건 추가
			if (request.gatheringId() != null) {
				predicates.add(criteriaBuilder.equal(gatheringJoin.get("id"), request.gatheringId()));
			}
			if (request.userId() != null) {
				predicates.add(criteriaBuilder.equal(userJoin.get("id"), request.userId()));
			}
			if (request.type() != null) {
				predicates.add(criteriaBuilder.equal(gatheringJoin.get("type"), request.type()));
			}
			if (request.location() != null) {
				predicates.add(criteriaBuilder.equal(gatheringJoin.get("location"), request.location()));
			}
			if (request.date() != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(gatheringJoin.get("dateTime"), request.date()));
			}
			if (request.registrationEnd() != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(gatheringJoin.get("registrationEnd"), request.registrationEnd()));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		// 데이터 조회
		Page<Review> reviewsPage = reviewRepository.findAll(specification, pageable);

		// ReviewResponse로 변환
		return reviewsPage.stream()
			.map(ReviewWithGatheringAndUserResponse::of)
			.toList();
	}



	public ReviewResponse createReview(ReviewServiceCreateRequest request) {
		Gathering gathering = getGatheringsFrom(request.gatheringId());
		User user = userRepository.findById(1L).orElse(null);

		checkIsUserParticipant(user, gathering);

		Review savedReview = reviewRepository.save(request.toEntity(gathering, user));
		return ReviewResponse.of(savedReview);
	}

	public List<ReviewScoreResponse> getReviewScores(List<Long> id, GatheringType type) {
		Specification<Review> specification = ((root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			// Gathering 조인
			Join<Review, Gathering> gatheringJoin = root.join("gathering", JoinType.INNER);

			if (id != null && !id.isEmpty()) {
				predicates.add(root.get("gathering").get("id").in(id));
			}

			if (type != null) {
				predicates.add(criteriaBuilder.equal(gatheringJoin.get("type"), type));
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		});

		// 데이터 조회
		List<Review> reviewsPage = reviewRepository.findAll(specification);

		Map<Gathering, Map<Integer, Long>> scoreCountsByGatheringId = reviewsPage.stream()
			.collect(Collectors.groupingBy(
				Review::getGathering,
				Collectors.groupingBy(
					Review::getScore,
					Collectors.counting()
				)
			));

		// ReviewResponse로 변환
		return scoreCountsByGatheringId.entrySet().stream()
			.map(entry -> {
				Gathering gathering = entry.getKey();

				Map<Integer, Long> scoreCounts = entry.getValue();
				// 점수별 카운트를 활용해 평균 계산
				long totalScore = scoreCounts.entrySet().stream()
					.mapToLong(e -> e.getKey() * e.getValue()) // score * count
					.sum();

				long totalReviews = scoreCounts.values().stream()
					.mapToLong(Long::longValue) // 리뷰 개수 합산
					.sum();

				double averageScore = totalReviews > 0 ? (double) totalScore / totalReviews : 0.0;

				return ReviewScoreResponse.of(gathering, averageScore, scoreCounts);
			})
			.toList();
	}

	private void checkIsUserParticipant(User user, Gathering gathering) {
		UserGatheringId userGatheringId = UserGatheringId.of(user, gathering);
		if (!userGatheringRepository.existsById(userGatheringId)) {
			throw new AppException(NOT_PARTICIPANTS);
		}
	}

	private Gathering getGatheringsFrom(Long id) {
		return gatheringRepository.findById(id).orElseThrow(() -> new AppException(NOT_FOUND));
	}
}
