package com.fesi.mukitlist.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fesi.mukitlist.domain.gathering.Gathering;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, JpaSpecificationExecutor<Gathering> {

}