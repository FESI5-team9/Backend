package com.fesi.mukitlist.api.repository.gathering;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fesi.mukitlist.api.repository.usergathering.UserGatheringSpecifications;
import com.fesi.mukitlist.api.service.gathering.request.GatheringServiceRequest;
import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.Gathering;
import com.fesi.mukitlist.domain.usergathering.UserGathering;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, JpaSpecificationExecutor<Gathering> {
	default Page<Gathering> findWithFilters(GatheringServiceRequest request, Pageable pageable) {
		Specification<Gathering> specification = Specification.where(GatheringSpecifications.byFilter(request));

		return this.findAll(specification, pageable);
	}
}