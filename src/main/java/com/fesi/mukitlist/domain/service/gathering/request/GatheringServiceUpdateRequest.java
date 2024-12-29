package com.fesi.mukitlist.domain.service.gathering.request;

import static com.fesi.mukitlist.api.exception.ExceptionCode.*;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fesi.mukitlist.api.exception.AppException;

import lombok.Builder;

@Builder
public record GatheringServiceUpdateRequest(
	String name,
	Integer openParticipantCount,
	Integer capacity,
	MultipartFile image,
	String description,
	List<String> keyword
) {
	public int minimumCapacity(Integer openParticipantCount) {
		if (capacity < openParticipantCount) throw new AppException(MINIMUM_CAPACITY);
		return capacity;
	}
}
