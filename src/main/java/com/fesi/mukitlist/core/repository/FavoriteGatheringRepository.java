package com.fesi.mukitlist.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fesi.mukitlist.core.gathering.favorite.FavoriteGathering;
import com.fesi.mukitlist.core.gathering.favorite.FavoriteGatheringId;

public interface FavoriteGatheringRepository extends JpaRepository<FavoriteGathering, FavoriteGatheringId> {
	List<FavoriteGathering> findById_UserId(Long userId);

	boolean existsByIdGatheringIdAndIdUserId(Long gatheringId, Long userId);
}
