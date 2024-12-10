package com.fesi.mukitlist.domain.gathering;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;
import static com.fesi.mukitlist.domain.gathering.constant.GatheringStatus.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fesi.mukitlist.api.controller.gathering.request.GatheringUpdateRequest;
import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.api.service.gathering.request.GatheringServiceCreateRequest;
import com.fesi.mukitlist.api.service.gathering.request.GatheringServiceUpdateRequest;
import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.constant.GatheringStatus;
import com.fesi.mukitlist.domain.gathering.constant.GatheringType;
import com.fesi.mukitlist.domain.gathering.constant.LocationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Gathering {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LocationType location;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GatheringType type;

	private String name;

	@Column(nullable = false)
	private LocalDateTime dateTime;

	@Column(nullable = false)
	private LocalDateTime registrationEnd;

	@Column(nullable = false)
	private String address1;

	@Column(nullable = false)
	private String address2;

	@Column
	private String description;

	@Column(nullable = false)
	private int openParticipantCount;

	@Column(nullable = false)
	private int participantCount = 0;

	@Column(nullable = false)
	private int capacity;

	private String image;

	@CreatedBy
	private String createdBy;

	@CreatedDate
	private LocalDateTime createdAt;

	private LocalDateTime canceledAt;
	@Enumerated(EnumType.STRING)
	private GatheringStatus status;

	@ManyToOne
	private User user;

	@Builder
	private Gathering(
		LocationType location,
		GatheringType type,
		String name,
		LocalDateTime dateTime,
		LocalDateTime registrationEnd,
		String address1,
		String address2,
		String description,
		int openParticipantCount,
		int participantCount,
		int capacity,
		String image,
		String createdBy,
		LocalDateTime createdAt,
		User user,
		LocalDateTime canceledAt,
		GatheringStatus status) {
		if (location == null || type == null || name == null || dateTime == null || address1 == null || address2 == null) {
			throw new AppException(REQUIRED_PROPERTIES);
		}
		this.location = location;
		this.type = type;
		this.name = name;
		this.dateTime = dateTime;
		this.registrationEnd = registrationEnd;
		this.address1 = address1;
		this.address2 = address2;
		this.description = description;
		this.participantCount = participantCount;
		this.openParticipantCount = openParticipantCount;
		this.capacity = capacity;
		this.image = image;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.user = user;
		this.canceledAt = canceledAt;
		this.status = status;
	}

	public static Gathering create(GatheringServiceCreateRequest request, String storedName, User user) {
		return Gathering.builder()
			.location(request.location())
			.type(request.type())
			.name(request.name())
			.dateTime(request.dateTime())
			.openParticipantCount(request.openParticipantCount())
			.capacity(request.minimumCapacity())
			.image(storedName)
			.registrationEnd(request.dateTime().minusHours(6))
			.address1(request.address1())
			.address2(request.address2())
			.description(request.description())
			.user(user)
			.status(RECRUITING)
			.build();
	}

	public Gathering update(GatheringServiceUpdateRequest request, String storedName) {
		this.location = request.location() != null ? request.location() : this.location;
		this.type = request.type() != null ? request.type() : this.type;
		this.name = request.name() != null ? request.name() : this.name;
		this.dateTime = request.dateTime() != null ? request.dateTime() : this.dateTime;

		if (request.openParticipantCount() != null && request.openParticipantCount() > 0) {
			this.openParticipantCount = request.openParticipantCount();
		}
		if (request.capacity() != null && request.capacity() > 0) {
			this.capacity = request.minimumCapacity();
		}
		this.image = storedName != null ? storedName : this.image;
		this.address1 = request.address1() != null ? request.address1() : this.address1;
		this.address2 = request.address2() != null ? request.address2() : this.address2;
		this.description = request.description() != null ? request.description() : this.description;

		return this;
	}

	public boolean isCancelAuthorization(User user) {
		return this.user.equals(user);
	}

	public boolean isHostUser(User user) {
		return this.user.equals(user);
	}

	public boolean isCanceledGathering() {
		return this.canceledAt != null;
	}

	public boolean isJoinableGathering() {
		return this.participantCount <= this.capacity;
	}

	public void joinParticipant() {
		this.participantCount++;
	}

	public void leaveParticipant() {
		this.participantCount--;
	}

	public void updateCanceledAt(LocalDateTime canceledTime) {
		this.canceledAt = canceledTime;
	}

	public void changeStatus(GatheringStatus status) {
		this.status = status;
	}
	// User
	// participants
	// Review
}
