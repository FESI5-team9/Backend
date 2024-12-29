package com.fesi.mukitlist.core.repository.usergathering;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.usergathering.UserGathering;
import com.fesi.mukitlist.core.usergathering.UserGatheringId;

public interface UserGatheringRepository
	extends JpaRepository<UserGathering, UserGatheringId>, JpaSpecificationExecutor<UserGathering> {
	default Page<UserGathering> findWithFilters(User user, Boolean completed, Boolean reviewed, Pageable pageable) {
		Specification<UserGathering> specification = Specification.where(UserGatheringSpecifications.byUser(user))
			.and(UserGatheringSpecifications.byCompleted(completed))
			.and(UserGatheringSpecifications.byReviewed(reviewed));

		return this.findAll(specification, pageable);
	}

	Page<UserGathering> findByIdGathering(Gathering gathering, Pageable pageable);

	List<UserGathering> findByIdGathering(Gathering gathering);

	boolean existsByIdUserAndIdGathering(User user, Gathering gathering);

	Set<UserGathering> findByIdIn(Set<UserGatheringId> userGatheringIds);
}