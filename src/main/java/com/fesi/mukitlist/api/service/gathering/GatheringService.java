package com.fesi.mukitlist.api.service.gathering;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.api.repository.KeywordRepository;
import com.fesi.mukitlist.api.repository.ReviewRepository;
import com.fesi.mukitlist.api.repository.UserRepository;
import com.fesi.mukitlist.api.repository.gathering.GatheringRepository;
import com.fesi.mukitlist.api.repository.usergathering.UserGatheringRepository;
import com.fesi.mukitlist.api.service.gathering.request.GatheringServiceCreateRequest;
import com.fesi.mukitlist.api.service.gathering.request.GatheringServiceRequest;
import com.fesi.mukitlist.api.service.gathering.response.GatheringCreateResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringListResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringParticipantsResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringResponse;
import com.fesi.mukitlist.api.service.gathering.response.JoinedGatheringsResponse;
import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.Gathering;
import com.fesi.mukitlist.domain.gathering.Keyword;
import com.fesi.mukitlist.domain.usergathering.UserGathering;
import com.fesi.mukitlist.domain.usergathering.UserGatheringId;
import com.fesi.mukitlist.global.aws.S3Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class GatheringService {
	private final GatheringRepository gatheringRepository;
	private final UserGatheringRepository userGatheringRepository;
	private final UserRepository userRepository;
	private final KeywordRepository keywordRepository;
	private final S3Service s3Service;

	@Transactional(readOnly = true)
	public List<GatheringListResponse> getGatherings(GatheringServiceRequest request, Pageable pageable) {

		Page<Gathering> gatheringPage = gatheringRepository.findWithFilters(request, pageable);

		return gatheringPage.stream()
			.map(GatheringListResponse::of)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<GatheringListResponse> searchGathering(List<String> search, Pageable pageable) {
		Page<Gathering> gatheringPage = gatheringRepository.searchByTerms(search, pageable);

		return gatheringPage.stream()
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

		List<Keyword> keywords = request.keyword().stream()
			.map(k -> Keyword.of(k, savedGathering))
			.collect(Collectors.toList());
		List<Keyword> savedKeywords = keywordRepository.saveAll(keywords);
		return GatheringCreateResponse.of(savedGathering, savedKeywords);
	}

	@Transactional(readOnly = true)
	public GatheringResponse getGatheringById(Long id, Long userId) {
		User user = getUserFrom(userId);
		Gathering gathering = getGatheringsFrom(id);
		List<Keyword> keywords = keywordRepository.findAllByGathering(gathering);
		return GatheringResponse.of(gathering, user, keywords);
	}

	public GatheringResponse cancelGathering(Long id, User user) {
		Gathering gathering = getGatheringsFrom(id);

		checkCancelAuthority(gathering, user);

		LocalDateTime canceledTime = LocalDateTime.now();
		gathering.updateCanceledAt(canceledTime);
		Gathering savedGathering = gatheringRepository.save(gathering);
		List<Keyword> savedKeywords = keywordRepository.findAllByGathering(gathering);

		return GatheringResponse.of(savedGathering, user, savedKeywords);
	}

	public void joinGathering(Long id, User user) {
		Gathering gathering = getGatheringsFrom(id);

		checkIsCanceledGathering(gathering);
		checkIsJoinedGathering(gathering);

		UserGatheringId userGatheringId = UserGatheringId.of(user, gathering);
		LocalDateTime joinedTime = LocalDateTime.now();
		UserGathering userGathering = UserGathering.of(userGatheringId, joinedTime);
		userGatheringRepository.save(userGathering);

		gathering.joinParticipant();
	}

	public void leaveGathering(Long id, User user, LocalDateTime leaveTime) {
		Gathering gathering = getGatheringsFrom(id);

		checkIsNotPastGathering(gathering, leaveTime);

		UserGatheringId userGatheringId = UserGatheringId.of(user, gathering);
		UserGathering userGathering = userGatheringRepository.findById(userGatheringId)
			.orElseThrow(() -> new AppException(NOT_PARTICIPANTS));
		userGatheringRepository.delete(userGathering);

		gathering.leaveParticipant();
		gatheringRepository.save(gathering);
	}

	public List<JoinedGatheringsResponse> getJoinedGatherings(User user, Boolean completed, Boolean reviewed,
		Pageable pageable) {
		Page<UserGathering> userGatheringPage = userGatheringRepository.findWithFilters(user, completed, reviewed,
			pageable);

		return userGatheringPage.stream()
			.map(userGathering -> JoinedGatheringsResponse.of(
				userGathering,
				completed,
				reviewed,
				keywordRepository.findAllByGathering(userGathering.getId()
					.getGathering())))
			.toList();
	}

	public List<GatheringParticipantsResponse> getGatheringParticipants(Long id, Pageable pageable) {
		Gathering gathering = getGatheringsFrom(id);
		List<UserGathering> gatherings = userGatheringRepository.findByIdGathering(gathering, pageable).getContent();
		return gatherings.stream()
			.map(GatheringParticipantsResponse::of)
			.toList();
	}

	private Gathering getGatheringsFrom(Long id) {
		return gatheringRepository.findById(id).orElseThrow(() -> new AppException(NOT_FOUND));
	}

	private User getUserFrom(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new AppException(NOT_FOUND_USER));
	}

	private void checkIsNotPastGathering(Gathering gathering, LocalDateTime leaveTime) {
		if (leaveTime.isAfter(gathering.getDateTime())) {
			throw new AppException(PAST_GATHERING);
		}
	}

	private void checkIsCanceledGathering(Gathering gathering) {
		if (gathering.isCanceledGathering()) {
			throw new AppException(GATHERING_CANCELED);
		}
	}

	private void checkIsJoinedGathering(Gathering gathering) {
		if (!gathering.isJoinableGathering()) {
			throw new AppException(MAXIMUM_PARTICIPANTS);
		}
	}

	private void checkCancelAuthority(Gathering gathering, User user) {
		if (!gathering.isCancelAuthorization(user)) {
			throw new AppException(FORBIDDEN);
		}
	}
}
