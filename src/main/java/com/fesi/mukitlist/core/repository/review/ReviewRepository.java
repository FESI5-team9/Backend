package com.fesi.mukitlist.core.repository.review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fesi.mukitlist.core.Review;
import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.gathering.constant.GatheringType;
import com.fesi.mukitlist.domain.service.review.request.ReviewServiceRequest;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {
	default Page<Review> findWithFilters(ReviewServiceRequest request, Pageable pageable) {
		Specification<Review> specification = Specification.where(ReviewSpecifications.byFilter(request));

		return findAll(specification, pageable);
	}

	default List<Review> findWithFilters(List<Long> id, GatheringType type) {
		Specification<Review> specification = Specification.where(ReviewSpecifications.ScoreByFilter(id, type));
		return findAll(specification);
	}

	List<Review> findAllByGathering_Type(GatheringType gatheringType);

	Page<Review> findAllByGatheringId(Long gatheringId, Pageable pageable);

	List<Review> findAllByUser(User user);

	boolean existsByUserAndGathering(User user, Gathering gathering);
}