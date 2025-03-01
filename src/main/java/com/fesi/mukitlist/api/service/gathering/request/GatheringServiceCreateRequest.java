package com.fesi.mukitlist.api.service.gathering.request;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fesi.mukitlist.domain.gathering.GatheringType;
import com.fesi.mukitlist.api.exception.AppException;

import lombok.Builder;

@Builder
public record GatheringServiceCreateRequest(
	String location,
	GatheringType type,
	String name,
	LocalDateTime dateTime,
	int capacity,
	// MultipartFile image
	LocalDateTime registrationEnd,
	String address1,
	String address2,
	String description,
	List<String> keyword
) {
	public int minimumCapacity() {
		if (capacity < 5) throw new AppException(MINIMUM_CAPACITY);
		return capacity;
	}
}
