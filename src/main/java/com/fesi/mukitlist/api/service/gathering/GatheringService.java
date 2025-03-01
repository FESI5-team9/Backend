package com.fesi.mukitlist.api.service.gathering;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.Gathering;
import com.fesi.mukitlist.domain.gathering.Keyword;
import com.fesi.mukitlist.domain.usergathering.UserGathering;
import com.fesi.mukitlist.domain.usergathering.UserGatheringId;
import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.api.repository.GatheringRepository;
import com.fesi.mukitlist.api.repository.KeywordRepository;
import com.fesi.mukitlist.api.repository.ReviewRepository;
import com.fesi.mukitlist.api.repository.usergathering.UserGatheringRepository;
import com.fesi.mukitlist.api.repository.UserRepository;
import com.fesi.mukitlist.api.service.gathering.request.GatheringServiceCreateRequest;
import com.fesi.mukitlist.api.service.gathering.request.GatheringServiceRequest;
import com.fesi.mukitlist.api.service.gathering.response.GatheringParticipantsResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringResponse;
import com.fesi.mukitlist.api.service.gathering.response.JoinedGatheringsResponse;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class GatheringService {

	private final GatheringRepository gatheringRepository;
	private final UserGatheringRepository userGatheringRepository;
	private final UserRepository userRepository;
	private final KeywordRepository keywordRepository;
	private final ReviewRepository reviewRepository;

	@Transactional(readOnly = true)
	public List<GatheringResponse> getGatherings(GatheringServiceRequest request, Pageable pageable) {

		Specification<Gathering> specification = ((root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(criteriaBuilder.isNull(root.get("canceledAt")));

			if (request.id() != null && !request.id().isEmpty()) {
				predicates.add(root.get("id").in(request.id()));
			}

			if (request.type() != null) {
				predicates.add(criteriaBuilder.equal(root.get("type"), request.type()));
			}

			if (request.location() != null) {
				predicates.add(criteriaBuilder.equal(root.get("location"), request.location()));
			}

			if (request.dateTime() != null) {
				predicates.add(criteriaBuilder.between(root.get("dateTime"), request.dateTime().toLocalDate()
					.atStartOfDay(), request.dateTime().plusDays(1).toLocalDate().atStartOfDay()));
			}

			if (request.createdBy() != null) {
				predicates.add(criteriaBuilder.equal(root.get("createdBy"), request.createdBy()));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		});

		Page<Gathering> gatheringPage = gatheringRepository.findAll(specification, pageable);

		return gatheringPage.stream()
			.map(g -> GatheringResponse.forList(g, keywordRepository.findAllByGathering(g)))
			.toList();
	}

	public GatheringResponse createGathering(GatheringServiceCreateRequest request) {
		User user = getUserFrom(1L);
		Gathering gathering = Gathering.create(request, user);
		Gathering savedGathering = gatheringRepository.save(gathering);

		List<Keyword> keywords = request.keyword().stream()
			.map(k -> Keyword.of(k, savedGathering))
			.collect(Collectors.toList());
		List<Keyword> savedKeywords = keywordRepository.saveAll(keywords);
		return GatheringResponse.forList(savedGathering, savedKeywords);
	}

	@Transactional(readOnly = true)
	public GatheringResponse getGatheringById(Long id) {
		Gathering gathering = getGatheringsFrom(id);
		List<Keyword> keywords = keywordRepository.findAllByGathering(gathering);
		return GatheringResponse.forDetail(gathering, keywords);
	}

	public GatheringResponse cancelGathering(Long id) {
		User user = getUserFrom(1L);
		Gathering gathering = getGatheringsFrom(id);

		checkCancelAuthority(gathering, user);

		LocalDateTime canceledTime = LocalDateTime.now();
		gathering.updateCanceledAt(canceledTime);
		Gathering savedGathering = gatheringRepository.save(gathering);
		List<Keyword> savedKeywords = keywordRepository.findAllByGathering(gathering);

		return GatheringResponse.forDetail(savedGathering, savedKeywords);
	}

	public void joinGathering(Long id) {
		Gathering gathering = getGatheringsFrom(id);
		User user = getUserFrom(1L);

		checkIsCanceledGathering(gathering);
		checkIsJoinedGathering(gathering);

		UserGatheringId userGatheringId = UserGatheringId.of(user, gathering);
		LocalDateTime joinedTime = LocalDateTime.now();
		UserGathering userGathering = UserGathering.of(userGatheringId, joinedTime);
		userGatheringRepository.save(userGathering);

		gathering.joinParticipant();
	}

	public void leaveGathering(Long id, LocalDateTime leaveTime) {
		Gathering gathering = getGatheringsFrom(id);
		User user = getUserFrom(1L);

		checkIsNotPastGathering(gathering, leaveTime);

		UserGatheringId userGatheringId = UserGatheringId.of(user, gathering);
		userGatheringRepository.deleteById(userGatheringId);

		gathering.leaveParticipant();
		gatheringRepository.save(gathering);
	}

	public List<JoinedGatheringsResponse> getJoinedGatherings(Long userId, Boolean completed, Boolean reviewed, Pageable pageable) {
		User user = getUserFrom(userId);
		Page<UserGathering> userGatheringPage = userGatheringRepository.findWithFilters(user,completed,reviewed, pageable);

		return userGatheringPage.stream()
			.map(userGathering -> JoinedGatheringsResponse.of(
				userGathering,
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

	private void checkIsNotPastGathering(Gathering gathering, LocalDateTime leaveTime) {
		if (leaveTime.isAfter(gathering.getDateTime())) {
			throw new AppException(PAST_GATHERING);
		}
	}

	private Gathering getGatheringsFrom(Long id) {
		return gatheringRepository.findById(id).orElseThrow(() -> new AppException(NOT_FOUND));
	}

	private User getUserFrom(Long id) {
		return userRepository.findById(id).orElse(null);
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
