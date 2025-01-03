package com.fesi.mukitlist.core.repository.gathering;

import static com.fesi.mukitlist.core.gathering.constant.GatheringType.*;
import static com.fesi.mukitlist.core.gathering.constant.LocationType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.fesi.mukitlist.core.auth.application.User;
import com.fesi.mukitlist.core.gathering.Gathering;
import com.fesi.mukitlist.core.repository.UserRepository;
import com.fesi.mukitlist.domain.service.gathering.request.GatheringServiceRequest;

@ActiveProfiles("test")
@DataJpaTest
class GatheringRepositoryTest {
	private static final LocalDateTime TEST_TIME = LocalDateTime.now().plusDays(1);

	@Autowired
	private GatheringRepository gatheringRepository;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {

		User user = User.builder()
			.email("test@test.com")
			.password("test1234!")
			.nickname("test")
			.createdAt(TEST_TIME)
			.updatedAt(TEST_TIME)
			.build();
		userRepository.save(user);

		Gathering gathering1 = Gathering.builder()
			.location(SEOUL)
			.type(CAFE)
			.name("성수동 카페")
			.dateTime(TEST_TIME)
			.capacity(5)
			.registrationEnd(TEST_TIME.minusHours(6))
			.address1("성동구")
			.address2("성수동")
			.description("성수동 카페 탐방")
			.createdAt(TEST_TIME)
			.createdBy("test")
			.user(user)
			.build();

		LocalDateTime gathering2DateTime = TEST_TIME.plusDays(1);
		Gathering gathering2 = Gathering.builder()
			.location(SEOUL)
			.type(CAFE)
			.name("자양동 카페")
			.dateTime(gathering2DateTime)
			.capacity(3)
			.registrationEnd(gathering2DateTime.minusHours(6))
			.address1("광진구")
			.address2("자양동")
			.description("자양동 카페 탐방")
			.createdAt(gathering2DateTime)
			.createdBy("test")
			.user(user)
			.build();
		gatheringRepository.saveAll(List.of(gathering1, gathering2));
	}

	@AfterEach
	void tearDown() {
		gatheringRepository.deleteAllInBatch();
		userRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("장소로 모임을 조회한다.")
	void findWithLocationTypeFilter() {
		// given
		GatheringServiceRequest request = GatheringServiceRequest.builder().location(SEOUL).build();
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("dateTime")));

		// when
		Page<Gathering> gatherings = gatheringRepository.findWithFilters(request, pageable);

		// then
		assertThat(gatherings.getContent()).hasSize(2)
			.extracting("name")
			.containsExactly("자양동 카페", "성수동 카페");
	}

	@Test
	@DisplayName("모임 종류로 조회한다.")
	void findWithGatheringTypeFilter() {
		// given
		GatheringServiceRequest request = GatheringServiceRequest.builder().type(CAFE).build();
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("dateTime")));

		// when
		Page<Gathering> gatherings = gatheringRepository.findWithFilters(request, pageable);

		// then
		assertThat(gatherings).isNotEmpty();
		assertThat(gatherings.getContent()).hasSize(2)
			.extracting("name")
			.containsExactly("자양동 카페", "성수동 카페");
	}

	@Test
	@DisplayName("검색어로 모임을 조회한다.")
	void searchByTerms() {
		// given
		String search = "성수동";

		// when
		PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("dateTime")));
		Page<Gathering> gatherings = gatheringRepository.searchByTerms(List.of(search), SEOUL, CAFE, pageable);

		// then
		assertThat(gatherings).isNotEmpty();
		assertThat(gatherings.getContent()).hasSize(1).extracting("name").containsExactly("성수동 카페");
	}

	@Test
	@DisplayName("주어진 Id 값에 맞는 모임을 전부 가져 온다.")
	void findAllByIdIn() {
		// given
		List<Long> gatheringCandidates = gatheringRepository.findAll().stream().map(Gathering::getId).toList();
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("dateTime")));

		// when
		Page<Gathering> gatherings = gatheringRepository.findAllByIdIn(gatheringCandidates, pageable);

		// then
		assertThat(gatherings).hasSize(2)
			.extracting("name").containsExactly("자양동 카페", "성수동 카페");
	}

	@Test
	@DisplayName("모임을 만든 사람으로 조회한다.")
	void findGatheringsByUser() {
		// given
		Long userId = userRepository.findByEmail("email").get().getId();
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("id")));

		// when
		List<Gathering> gatherings = gatheringRepository.findGatheringsByUserId(userId, pageable);

		// then
		assertThat(gatherings).hasSize(2).extracting("createdBy").containsExactly("test", "test");
	}

}