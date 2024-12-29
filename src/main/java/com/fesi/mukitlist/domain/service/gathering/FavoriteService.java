package com.fesi.mukitlist.domain.service.gathering;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.gathering.favorite.FavoriteGathering;
import com.fesi.mukitlist.core.gathering.favorite.FavoriteGatheringId;
import com.fesi.mukitlist.core.repository.FavoriteGatheringRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class FavoriteService {

	private final FavoriteGatheringRepository favoriteGatheringRepository;

	@Transactional(readOnly = true)
	public Set<Long> getFavoritedGatheringsBy(List<Gathering> gatheringList, User user ) {
		Set<FavoriteGatheringId> favoriteGatheringCandidates = gatheringList.stream()
			.map(gathering -> FavoriteGatheringId.of(user.getId(), gathering.getId()))
			.collect(Collectors.toSet());
		return favoriteGatheringRepository.findByIdIn(favoriteGatheringCandidates)
			.stream()
			.map(favoriteGathering -> favoriteGathering.getId().getGatheringId())
			.collect(Collectors.toSet());
	}

	public boolean isFavorite(Gathering gathering, User user) {
		boolean isFavorite = false;
		if (user != null) {
			isFavorite = checkIsFavoriteGathering(gathering, user);
		}
		return isFavorite;
	}

	private boolean checkIsFavoriteGathering(Gathering gathering, User user) {
		return favoriteGatheringRepository.existsById(FavoriteGatheringId.of(user.getId(), gathering.getId()));
	}

	public void markAsFavorite(Gathering gathering, User user) {
		favoriteGatheringRepository.save(
			FavoriteGathering.of(FavoriteGatheringId.of(user.getId(), gathering.getId())));
	}

	public void unmarkAsFavorite(Gathering gathering, User user) {
		favoriteGatheringRepository.delete(
			FavoriteGathering.of(FavoriteGatheringId.of(user.getId(), gathering.getId())));
	}

	public List<Long> findFavoriteGatheringIdBy(Long userId) {
		return favoriteGatheringRepository.findById_UserId(userId)
			.stream()
			.map(favoriteGathering -> favoriteGathering.getId().getGatheringId())
			.collect(Collectors.toList());
	}
}
