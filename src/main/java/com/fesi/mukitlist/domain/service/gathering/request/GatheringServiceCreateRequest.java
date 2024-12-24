package com.fesi.mukitlist.domain.service.gathering.request;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fesi.mukitlist.core.gathering.constant.GatheringType;
import com.fesi.mukitlist.api.exception.AppException;
import com.fesi.mukitlist.core.gathering.constant.LocationType;

public record GatheringServiceCreateRequest(
	LocationType location,
	GatheringType type,
	String name,
	LocalDateTime dateTime,
	int openParticipantCount,
	int capacity,
	MultipartFile image,
	String address1,
	String address2,
	String description,
	List<String> keyword
) {
	public GatheringServiceCreateRequest {
		if (dateTime.isBefore(LocalDateTime.now())) throw new AppException(PAST_GATHERING);
		if (dateTime.isAfter(LocalDateTime.now().plusDays(61))) throw new AppException(FUTURE_GATHERING);
		if (capacity < openParticipantCount) throw new AppException(MINIMUM_CAPACITY);
	}
}
