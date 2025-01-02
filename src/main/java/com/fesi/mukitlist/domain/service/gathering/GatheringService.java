package com.fesi.mukitlist.domain.service.gathering;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fesi.mukitlist.api.controller.gathering.request.ChangeGatheringStautsRequest;
import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.gathering.Keyword;
import com.fesi.mukitlist.core.gathering.constant.GatheringType;
import com.fesi.mukitlist.core.gathering.constant.LocationType;
import com.fesi.mukitlist.core.repository.FavoriteGatheringRepository;
import com.fesi.mukitlist.core.repository.gathering.GatheringRepository;
import com.fesi.mukitlist.core.usergathering.UserGathering;
import com.fesi.mukitlist.domain.service.aws.S3Service;
import com.fesi.mukitlist.domain.service.gathering.request.GatheringServiceCreateRequest;
import com.fesi.mukitlist.domain.service.gathering.request.GatheringServiceRequest;
import com.fesi.mukitlist.domain.service.gathering.request.GatheringServiceUpdateRequest;
import com.fesi.mukitlist.domain.service.gathering.response.GatheringCreateResponse;
import com.fesi.mukitlist.domain.service.gathering.response.GatheringListResponse;
import com.fesi.mukitlist.domain.service.gathering.response.GatheringParticipantsResponse;
import com.fesi.mukitlist.domain.service.gathering.response.GatheringResponse;
import com.fesi.mukitlist.domain.service.gathering.response.GatheringUpdateResponse;
import com.fesi.mukitlist.domain.service.gathering.response.GatheringWithParticipantsResponse;
import com.fesi.mukitlist.domain.service.gathering.response.JoinedGatheringsResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class GatheringService {
	private final GatheringRepository gatheringRepository;
	private final ParticipationService participationService;
	private final KeywordService keywordService;
	private final FavoriteService favoriteService;
	private final S3Service s3Service;
	private final FavoriteGatheringRepository favoriteGatheringRepository;

	@Transactional(readOnly = true)
	public List<GatheringListResponse> getGatherings(GatheringServiceRequest request, User user, Pageable pageable) {
		Page<Gathering> gatheringPage = gatheringRepository.findWithFilters(request, pageable);
		if (user == null) {
			return gatheringPage.stream()
				.map(GatheringListResponse::of)
				.toList();
		}
		return gatheringResponseBy(user, gatheringPage);
	}

	@Transactional(readOnly = true)
	public List<GatheringListResponse> searchGathering(List<String> search, User user, LocationType location,
		GatheringType type, Pageable pageable) {
		Page<Gathering> gatheringPage = gatheringRepository.searchByTerms(search, location, type, pageable);
		if (user == null) {
			return gatheringPage.stream()
				.map(GatheringListResponse::of)
				.toList();
		}
		return gatheringResponseBy(user, gatheringPage);
	}

	@Transactional(readOnly = true)
	public List<GatheringListResponse> getGatheringsByUserId(Long userId, Pageable pageable) {
		List<Gathering> response = gatheringRepository.findGatheringsByUserId(userId, pageable);
		return response.stream()
			.map(GatheringListResponse::of)
			.toList();
	}

	public GatheringCreateResponse createGathering(GatheringServiceCreateRequest request, User user) throws
		IOException {

		String storedName = "";
		if (request.image() != null) {
			storedName = s3Service.upload(request.image(), request.image().getOriginalFilename());
		}

		Gathering gathering = Gathering.create(request, storedName, user);
		Gathering savedGathering = gatheringRepository.save(gathering);

		participationService.joinGathering(savedGathering, user, gathering.getCreatedAt());

		List<Keyword> savedKeywords = new ArrayList<>();
		if (request.keyword() != null) {
		 savedKeywords = keywordService.saveKeywords(request.keyword(), gathering);
		}

		return GatheringCreateResponse.of(savedGathering, savedKeywords);
	}

	public GatheringUpdateResponse updateGathering(Long id, GatheringServiceUpdateRequest request, User user) throws
		IOException {

		String storedName = "";
		Gathering gathering = getGatheringsFrom(id);

		if (request.image() == null) {
			storedName = gathering.getImage();
		} else if (Objects.equals(request.image().getOriginalFilename(), "")) {
			storedName = "";
		} else {
			storedName = s3Service.upload(request.image(), request.image().getOriginalFilename());
		}

		checkUpdateAuthority(gathering, user);

		Gathering savedGathering = gatheringRepository.save(gathering.update(request, storedName));

		keywordService.updateKeywords(request.keyword(), savedGathering);
		List<Keyword> savedKeywords = keywordService.findByGathering(savedGathering);

		return GatheringUpdateResponse.of(savedGathering, savedKeywords);
	}

	@Transactional(readOnly = true)
	public GatheringWithParticipantsResponse getGatheringById(Long id, User user) {

		Gathering gathering = getGatheringsFrom(id);
		List<GatheringParticipantsResponse> participants = getGatheringsWithParticipantsFrom(gathering);
		List<Keyword> keywords = keywordService.findByGathering(gathering);
		boolean isFavorite = checkIsFavoriteGathering(gathering, user);

		return GatheringWithParticipantsResponse.of(gathering, user, keywords, isFavorite, participants);
	}

	public GatheringResponse cancelGathering(Long id, User user) {
		Gathering gathering = getGatheringsFrom(id);

		checkCancelAuthority(gathering, user);

		LocalDateTime canceledTime = LocalDateTime.now();
		checkCancelAvailability(gathering, canceledTime);

		Gathering savedGathering = gatheringRepository.save(gathering);
		List<Keyword> savedKeywords = keywordService.findByGathering(gathering);

		return GatheringResponse.of(savedGathering, user, savedKeywords);
	}

	public void joinGathering(Long id, User user) {
		Gathering gathering = getGatheringsFrom(id);

		LocalDateTime joinedTime = LocalDateTime.now();

		participationService.checkAlreadyJoinedGathering(gathering, user);
		participationService.joinGathering(gathering, user, joinedTime);
	}

	public void leaveGathering(Long id, User user, LocalDateTime leaveTime) {
		Gathering gathering = getGatheringsFrom(id);
		checkGatheringHost(gathering, user);

		participationService.checkAlreadyLeavedGathering(gathering, user);
		participationService.leaveGathering(gathering, user, leaveTime);
	}

	@Transactional(readOnly = true)
	public List<JoinedGatheringsResponse> getJoinedGatherings(User user, Boolean completed, Boolean reviewed,
		Pageable pageable) {

		Page<UserGathering> participantsWithFilters = participationService.getParticipantsWithFilters(user, completed,
			reviewed, pageable);

		return participantsWithFilters.stream()
			.map(userGathering -> JoinedGatheringsResponse.of(
				userGathering,
				completed,
				reviewed,
				keywordService.findByGathering(userGathering.getId().getGathering())))
			.toList();
	}

	@Transactional(readOnly = true)
	public List<GatheringParticipantsResponse> getGatheringParticipants(Long id, Pageable pageable) {

		Gathering gathering = getGatheringsFrom(id);
		List<UserGathering> gatherings = participationService.getParticipantsBy(gathering, pageable);
		return gatherings.stream()
			.map(GatheringParticipantsResponse::of)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<GatheringListResponse> findFavoriteGatheringsBy(User user, Pageable pageable) {

		List<Long> gatheringCandidates = favoriteService.findFavoriteGatheringIdBy(user.getId());
		Page<Gathering> gatherings = gatheringRepository.findAllByIdIn(gatheringCandidates, pageable);

		return gatheringResponseBy(user, gatherings);
	}

	public void choiceFavorite(Long id, User user) {
		Gathering gathering = getGatheringsFrom(id);
		checkHostUserMarkFavorite(user, gathering);
		checkIsAlreadyFavoriteGathering(gathering, user);
		favoriteService.markAsFavorite(gathering, user);
	}

	public void cancelFavorite(Long id, User user) {
		Gathering gathering = getGatheringsFrom(id);
		checkIsAlreadyCanceledFavoriteGathering(gathering, user);
		favoriteService.unmarkAsFavorite(gathering, user);
	}

	public Map<String, String> changeGatheringStatus(Long id, ChangeGatheringStautsRequest request, User user) {
		Gathering gathering = getGatheringsFrom(id);
		checkChangeStatusPermisson(user, gathering);
		gathering.changeStatus(request.status());
		return Map.of("모임 상태 변경", request.status().getDescription());
	}

	private List<GatheringListResponse> gatheringResponseBy(User user, Page<Gathering> gatheringPage) {
		Map<Long, GatheringListResponse> gatheringMap = gatheringPage.stream()
			.collect(Collectors.toMap(Gathering::getId, GatheringListResponse::of, (a, b) -> a, LinkedHashMap::new));
		List<Gathering> gatheringList = gatheringPage.getContent();

		updateParticipationStatus(user, gatheringList, gatheringMap);
		updateFavoriteStatus(user, gatheringList, gatheringMap);
		List<GatheringListResponse> responses = new ArrayList<>(gatheringMap.values());
		return responses;
	}

	private void updateFavoriteStatus(User user, List<Gathering> gatheringList,
		Map<Long, GatheringListResponse> gatheringMap) {
		favoriteService.getFavoritedGatheringsBy(gatheringList, user)
			.forEach(id -> gatheringMap.get(id).updateFavoriteStatusTrue());
	}

	private void updateParticipationStatus(User user, List<Gathering> gatheringList,
		Map<Long, GatheringListResponse> gatheringMap) {
		participationService.getParticipatedGatheringsBy(gatheringList, user)
			.forEach(gathering -> gatheringMap.get(gathering.getId()).updateParticipationStatusTrue());
	}

	private boolean checkIsFavoriteGathering(Gathering gathering, User user) {
		return favoriteService.isFavorite(gathering, user);
	}

	private List<GatheringParticipantsResponse> getGatheringsWithParticipantsFrom(Gathering gathering) {
		List<UserGathering> userGathering = participationService.getParticipantsBy(gathering);
		return userGathering.stream()
			.map(GatheringParticipantsResponse::of
			)
			.collect(Collectors.toList());
	}

	private Gathering getGatheringsFrom(Long id) {
		return gatheringRepository.findById(id).orElseThrow(() -> new AppException(NOT_FOUND));
	}

	private void checkGatheringHost(Gathering gathering, User user) {
		if (gathering.isHostUser(user)) {
			throw new AppException(HOST_CANNOT_LEAVE);
		}
	}

	private void checkCancelAuthority(Gathering gathering, User user) {
		if (!gathering.isHostUser(user)) {
			throw new AppException(FORBIDDEN);
		}
	}

	private void checkCancelAvailability(Gathering gathering, LocalDateTime canceledTime) {
		if (!gathering.updateCanceledAt(canceledTime)) {
			throw new AppException(CANCEL_GATHERING_NOT_AVAILABLE);
		}
	}

	private void checkUpdateAuthority(Gathering gathering, User user) {
		if (!gathering.isHostUser(user)) {
			throw new AppException(FORBIDDEN);
		}
	}

	private void checkHostUserMarkFavorite(User user, Gathering gathering) {
		if (gathering.isHostUser(user)) {
			throw new AppException(HOST_CANNOT_FAVORITE);
		}
	}

	private void checkChangeStatusPermisson(User user, Gathering gathering) {
		if (gathering.getUser().getId().equals(user.getId())) {
			throw new AppException(FORBIDDEN);
		}
	}

	private void checkIsAlreadyFavoriteGathering(Gathering gathering, User user) {
		if (favoriteGatheringRepository.existsByIdGatheringIdAndIdUserId(gathering.getId(), user.getId())) {
			throw new AppException(ALREADY_FAVORITE_GATHERING);
		}
	}

	private void checkIsAlreadyCanceledFavoriteGathering(Gathering gathering, User user) {
		if (!favoriteGatheringRepository.existsByIdGatheringIdAndIdUserId(gathering.getId(), user.getId())) {
			throw new AppException(ALREADY_CANCELED_FAVORITE_GATHERING);
		}
	}

}
