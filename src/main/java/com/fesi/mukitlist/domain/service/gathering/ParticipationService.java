package com.fesi.mukitlist.domain.service.gathering;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.repository.usergathering.UserGatheringRepository;
import com.fesi.mukitlist.core.usergathering.UserGathering;
import com.fesi.mukitlist.core.usergathering.UserGatheringId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ParticipationService {
	private final UserGatheringRepository userGatheringRepository;

	public void checkAlreadyJoinedGathering(Gathering gathering, User user) {
		checkIsAlreadyJoinedUser(gathering, user);
	}

	public void checkAlreadyLeavedGathering(Gathering gathering, User user) {
		checkIsAlreadyLeavedUser(gathering, user);
	}

	public Gathering joinGathering(Gathering gathering, User user, LocalDateTime joinedTime) {
		checkIsNotPastGathering(gathering, joinedTime);
		checkIsCanceledGathering(gathering);
		checkIsJoinedGathering(gathering);

		UserGatheringId userGatheringId = UserGatheringId.of(user, gathering);
		UserGathering userGathering = UserGathering.of(userGatheringId, joinedTime);
		userGatheringRepository.save(userGathering);
		gathering.updateParticipantCount(getParticipantCount(gathering));
		return gathering;
	}

	public Gathering leaveGathering(Gathering gathering, User user, LocalDateTime leaveTime) {
		checkIsNotPastGathering(gathering, leaveTime);
		checkIsCanceledGathering(gathering);

		UserGatheringId userGatheringId = UserGatheringId.of(user, gathering);
		UserGathering userGathering = userGatheringRepository.findById(userGatheringId)
			.orElseThrow(() -> new AppException(NOT_PARTICIPANTS));
		userGatheringRepository.delete(userGathering);
		gathering.updateParticipantCount(getParticipantCount(gathering));

		return gathering;
	}

	public List<UserGathering> getParticipantsBy(Gathering gathering) {
		return userGatheringRepository.findByIdGathering(gathering);
	}

	public List<UserGathering> getParticipantsBy(Gathering gathering, Pageable pageable) {
		return userGatheringRepository.findByIdGathering(gathering, pageable).getContent();
	}

	public List<UserGathering> getParticipantsBy(User user) {
		return userGatheringRepository.findByIdUser(user);
	}

	public Page<UserGathering> getParticipantsWithFilters(User user, Boolean completed, Boolean reviewed,
		Pageable pageable) {
		return userGatheringRepository.findWithFilters(user, completed, reviewed, pageable);
	}

	private void checkIsAlreadyJoinedUser(Gathering gathering, User user) {
		if (userGatheringRepository.existsByIdUserAndIdGathering(user, gathering)) {
			throw new AppException(ALREADY_JOINED_GATHERING);
		}
	}

	private void checkIsAlreadyLeavedUser(Gathering gathering, User user) {
		if (!userGatheringRepository.existsByIdUserAndIdGathering(user, gathering)) {
			throw new AppException(ALREADY_LEAVED_GATHERING);
		}
	}

	private void checkIsNotPastGathering(Gathering gathering, LocalDateTime time) {
		if (time.isAfter(gathering.getDateTime())) {
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

	private int getParticipantCount(Gathering gathering) {
		return userGatheringRepository.findByIdGathering(gathering).size();
	}

}
