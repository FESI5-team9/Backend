package com.fesi.mukitlist.api.controller.gathering;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fesi.mukitlist.api.controller.gathering.request.GatheringCreateRequest;
import com.fesi.mukitlist.api.controller.gathering.request.GatheringRequest;
import com.fesi.mukitlist.api.controller.gathering.request.GatheringUpdateRequest;
import com.fesi.mukitlist.api.exception.response.ValidationErrorResponse;
import com.fesi.mukitlist.api.service.gathering.GatheringService;
import com.fesi.mukitlist.api.service.gathering.response.GatheringCreateResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringListResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringParticipantsResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringUpdateResponse;
import com.fesi.mukitlist.api.service.gathering.response.GatheringWithParticipantsResponse;
import com.fesi.mukitlist.api.service.gathering.response.JoinedGatheringsResponse;
import com.fesi.mukitlist.domain.auth.PrincipalDetails;
import com.fesi.mukitlist.domain.auth.User;
import com.fesi.mukitlist.domain.gathering.constant.GatheringStatus;
import com.fesi.mukitlist.domain.gathering.constant.GatheringType;
import com.fesi.mukitlist.domain.gathering.constant.LocationType;
import com.fesi.mukitlist.global.annotation.Authorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/gatherings")
@Tag(name = "Gatherings", description = "모임 관련 API")
@Slf4j
public class GatheringController {
	private final GatheringService gatheringService;

	@Operation(summary = "모임 목록 조회", description = "모임의 종류, 위치, 날짜 등 다양한 조건으로 모임 목록을 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공",
				content = @Content(array = @ArraySchema(
					schema = @Schema(implementation = GatheringListResponse.class)
				))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))),
		}
	)
	@GetMapping
	ResponseEntity<List<GatheringListResponse>> getGatherings(
		@ParameterObject @ModelAttribute GatheringRequest request,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "0") int page,
		@Schema(description = "정렬 기준", example = "dateTime(모임일), registrationEnd(모집 마감일), participantCount(참여 인원)", minimum = "5")
		@RequestParam(defaultValue = "dateTime") String sort,
		@RequestParam(defaultValue = "desc") String direction
	) {
		Sort sortOrder = Sort.by(Sort.Order.by(sort).with(Sort.Direction.fromString(direction)));
		Pageable pageable = PageRequest.of(page, size, sortOrder);
		return new ResponseEntity<>(gatheringService.getGatherings(request.toServiceRequest(), pageable),
			HttpStatus.OK);
	}

	@Operation(summary = "모임 목록 검색", description = "모임의 종류, 위치, 날짜 등 다양한 조건으로 모임 목록을 검색합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공",
				content = @Content(array = @ArraySchema(
					schema = @Schema(implementation = GatheringListResponse.class)
				))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))),
		}
	)
	@GetMapping("/search")
	ResponseEntity<List<GatheringListResponse>> searchGatherings(
		@RequestParam List<String> search,
		@RequestParam(required = false) LocationType location,
		@RequestParam(required = false) GatheringType type,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "dateTime") String sort,
		@RequestParam(defaultValue = "desc") String direction
	) {

		Sort sortOrder = Sort.by(Sort.Order.by(sort).with(Sort.Direction.fromString(direction)));
		Pageable pageable = PageRequest.of(page, size, sortOrder);
		return new ResponseEntity<>(gatheringService.searchGathering(search, location, type, pageable), HttpStatus.OK);
	}

	@Operation(summary = "모임 상세 조회", description = "모임의 상제 정보를 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 상세 조회 성공",
				content = @Content(schema = @Schema(implementation = GatheringWithParticipantsResponse.class))),
			@ApiResponse(
				responseCode = "400",
				description = "요청 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"VALIDATION_ERROR\",\"parameter\":\"id\",\"message\":\"유효한 모임 ID를 입력하세요\"}"
					)
				)
			),
			@ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음",
				content = @Content(schema = @Schema(
					example = "{\"code\":\"NOT_FOUND\",\"message\":\"모임을 찾을 수 없습니다\"}"
				))),
		}
	)
	@GetMapping("/{id}")
	ResponseEntity<GatheringWithParticipantsResponse> getGatheringById(@PathVariable("id") Long id,
		@Parameter(hidden = true) @Authorize(required = false) PrincipalDetails user) {
		return new ResponseEntity<>(gatheringService.getGatheringById(id, user != null ? user.getUser() : null), HttpStatus.OK);
	}

	@Operation(summary = "모임 상태 변경", description = "모임의 상태를 변경합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 상태 변경 성공"),
			@ApiResponse(
				responseCode = "400",
				description = "요청 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"VALIDATION_ERROR\",\"parameter\":\"id\",\"message\":\"유효한 모임 ID를 입력하세요\"}"
					)
				)
			),
			@ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음",
				content = @Content(schema = @Schema(
					example = "{\"code\":\"NOT_FOUND\",\"message\":\"모임을 찾을 수 없습니다\"}"
				))),
		}
	)
	@GetMapping("/{id}/recruit")
	ResponseEntity<Map<String, String>> getGatheringRecruit(@PathVariable("id") Long id,
		@RequestParam GatheringStatus status,
		@Parameter(hidden = true) @Authorize PrincipalDetails user) {
		return new ResponseEntity<>(gatheringService.changeGatheringStatus(id,status,user.getUser()), HttpStatus.OK);

	}

	@Operation(summary = "특정 모임의 참가자 목록 조회", description = "특정 모임의 참가자 목록을 페이지네이션 하여 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공",
				content = @Content(
					mediaType = "application/json",
					array = @ArraySchema(
						schema = @Schema(implementation = GatheringParticipantsResponse.class)
					)
				)),
			@ApiResponse(
				responseCode = "400",
				description = "요청 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"VALIDATION_ERROR\",\"parameter\":\"id\",\"message\":\"유효한 모임 ID를 입력하세요\"}"
					)
				)
			),
			@ApiResponse(
				responseCode = "404",
				description = "모임 없음",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"NOT_FOUND\",\"message\":\"모임을 찾을 수 없습니다.\"}"
					)
				)
			),
		}
	)
	@GetMapping("/{id}/participants")
	ResponseEntity<List<GatheringParticipantsResponse>> getGatheringParticipantsById(
		@PathVariable("id") Long id,
		@RequestParam(defaultValue = "5") int size,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "joinedAt") String sort,
		@RequestParam(defaultValue = "desc") String direction) {

		Sort sortOrder = Sort.by(Sort.Order.by(sort).with(Sort.Direction.fromString(direction)));
		Pageable pageable = PageRequest.of(page, size, sortOrder);
		return new ResponseEntity<>(gatheringService.getGatheringParticipants(id, pageable), HttpStatus.OK);
	}

	@Operation(summary = "모임 생성", description = "새로운 모임 생성",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "201", description = "모임 생성 성공",
				content = @Content(
					schema = @Schema(implementation = GatheringCreateResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))),
		}
	)
	@PostMapping(consumes = "multipart/form-data")
	public ResponseEntity<GatheringCreateResponse> createGathering(
		@ModelAttribute @Valid GatheringCreateRequest request,
		@Parameter(hidden = true) @Authorize PrincipalDetails user) throws IOException {
		return new ResponseEntity<>(gatheringService.createGathering(request.toServiceRequest(),
			user.getUser()), HttpStatus.CREATED);
	}

	@Operation(summary = "모임 수정", description = "모임 수정",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 수정 성공",
				content = @Content(
					schema = @Schema(implementation = GatheringUpdateResponse.class))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))),
		}
	)
	@PutMapping(value = "/{id}", consumes = "multipart/form-data")
	public ResponseEntity<GatheringUpdateResponse> updateGathering(
		@PathVariable("id") Long id,
		@ModelAttribute @Valid GatheringUpdateRequest request,
		@Parameter(hidden = true) @Authorize PrincipalDetails user) throws IOException {
		return new ResponseEntity<>(gatheringService.updateGathering(id,request.toServiceRequest(),
			user.getUser()), HttpStatus.OK);
	}

	@Operation(summary = "로그인된 사용자가 참석한 모임 목록 조회", description = "로그인된 사용자가 참석한 모임의 목록을 조회합니다.",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공",
				content = @Content(
					mediaType = "application/json",
					array = @ArraySchema(
						schema = @Schema(implementation = JoinedGatheringsResponse.class)
					)
				)),
			@ApiResponse(
				responseCode = "400",
				description = "요청 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"VALIDATION_ERROR\",\"parameter\":\"size\",\"message\":\"size 는 최소 1이어야 합니다.\"}"
					)
				)
			),
		}
	)
	@GetMapping("/joined")
	public ResponseEntity<List<JoinedGatheringsResponse>> getGatheringsBySignInUser(
		@Parameter(hidden = true) @Authorize PrincipalDetails user,
		@RequestParam(required = false) Boolean completed,
		@RequestParam(required = false) Boolean reviewed,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "id.gathering.dateTime") String sort,
		@RequestParam(defaultValue = "desc") String direction) {

		Sort sortOrder = Sort.by(Sort.Order.by(sort).with(Sort.Direction.fromString(direction)));
		Pageable pageable = PageRequest.of(page, size, sortOrder);

		return new ResponseEntity<>(gatheringService.getJoinedGatherings(user.getUser(), completed,
			reviewed, pageable), HttpStatus.OK);
	}

	@Operation(summary = "모임 취소", description = "모임을 취소합니다. 모임 생성자만 취소할 수 있습니다.",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 취소 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = GatheringResponse.class))),
			@ApiResponse(
				responseCode = "403",
				description = "권한 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"FORBIDDEN\",\"message\":\"모임을 취소할 권한이 없습니다\"}"
					)
				)
			),
			@ApiResponse(
				responseCode = "404",
				description = "모임 없음",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"NOT_FOUND\",\"message\":\"모임을 찾을 수 없습니다.\"}"
					)
				)
			),
		}
	)
	@PutMapping("/{id}/cancel")
	public ResponseEntity<GatheringResponse> cancelGathering(@PathVariable("id") Long id,
		@Parameter(hidden = true) @Authorize User user) {
		return new ResponseEntity<>(gatheringService.cancelGathering(id, user), HttpStatus.OK);
	}

	@Operation(summary = "모임 참여", description = "로그인한 사용자가 모임에 참여합니다",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 참여 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"message\":\"모임에 참여했습니다.\"}"
					)
				)),
			@ApiResponse(
				responseCode = "400",
				description = "참여 불가 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"GATHERING_CANCELED\",\"message\":\"취소된 모임입니다.\"}"
					)
				)
			),
			@ApiResponse(
				responseCode = "404",
				description = "모임 없음",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"NOT_FOUND\",\"message\":\"모임을 찾을 수 없습니다.\"}"
					)
				)
			),
		}
	)
	@PostMapping("/{id}/join")
	public ResponseEntity<Map<String, String>> joinGathering(@PathVariable("id") Long id,
		@Parameter(hidden = true) @Authorize PrincipalDetails user) {
		gatheringService.joinGathering(id, user.getUser());
		return new ResponseEntity<>(Map.of("message", "모임에 참여했습니다."), HttpStatus.OK);
	}

	@Operation(summary = "모임 참여 취소", description = "사용자가 모임에서 참여 취소합니다.이미 지난 모임은 참여 취소가 불가합니다.",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 참여 취소 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"message\":\"모임에 참여했습니다.\"}"
					)
				)),
			@ApiResponse(
				responseCode = "400",
				description = "참여 불가 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"PAST_GATHERING\",\"message\":\"이미 지난 모임입니다\"}"
					)
				)
			),
			@ApiResponse(
				responseCode = "404",
				description = "모임 없음",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"NOT_FOUND\",\"message\":\"모임을 찾을 수 없습니다.\"}"
					)
				)
			),
		}
	)
	@DeleteMapping("/{id}/leave")
	public ResponseEntity<Map<String, String>> leaveGathering(@PathVariable("id") Long id,
		@Parameter(hidden = true) @Authorize PrincipalDetails user) {
		LocalDateTime leaveTime = LocalDateTime.now();
		gatheringService.leaveGathering(id, user.getUser(), leaveTime);
		return new ResponseEntity<>(Map.of("message", "모임 참여 취소를 성공했습니다."), HttpStatus.OK);
	}

	@Operation(summary = "찜한 모임 목록 조회", description = "찜한 모임 목록을 조회합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 목록 조회 성공",
				content = @Content(array = @ArraySchema(
					schema = @Schema(implementation = GatheringListResponse.class)
				))),
			@ApiResponse(responseCode = "400", description = "잘못된 요청",
				content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))),
		}
	)
	@GetMapping("/favorite")
	public ResponseEntity<List<GatheringListResponse>> getFavoriteGatherings(
		@Parameter(hidden = true) @Authorize User user) {

		List<GatheringListResponse> response = gatheringService.findFavoriteGatheringsBy(user);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Operation(summary = "모임 찜하기", description = "사용자가 모임을 찜합니다.이미 지난 모임은 찜하기가 불가합니다.",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 찜하기 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"message\":\"모임 찜하기를 성공했습니다.\"}"
					)
				)),
			@ApiResponse(
				responseCode = "400",
				description = "찜하기 불가 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"PAST_GATHERING\",\"message\":\"이미 지난 모임입니다\"}"
					)
				)
			),
			@ApiResponse(
				responseCode = "404",
				description = "모임 없음",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"NOT_FOUND\",\"message\":\"모임을 찾을 수 없습니다.\"}"
					)
				)
			),
		}
	)
	@PostMapping("/{id}/favorite")
	public ResponseEntity<Map<String, String>> choiceFavoriteGathering(@PathVariable("id") Long id,
		@Parameter(hidden = true) @Authorize User user) {

		gatheringService.choiceFavorite(id, user);
		return new ResponseEntity<>(Map.of("message", "모임 찜하기를 성공했습니다."), HttpStatus.OK);
	}

	@Operation(summary = "모임 찜하기 취소하기", description = "사용자가 찜한 모임을 취소 합니다 .이미 지난 모임은 찜하기가 불가합니다.",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "200", description = "모임 찜 취소 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"message\":\"모임 찜하기를 취소했습니다.\"}"
					)
				)),
			@ApiResponse(
				responseCode = "400",
				description = "찜하기 불가 오류",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"PAST_GATHERING\",\"message\":\"이미 지난 모임입니다\"}"
					)
				)
			),
			@ApiResponse(
				responseCode = "404",
				description = "모임 없음",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(
						example = "{\"code\":\"NOT_FOUND\",\"message\":\"모임을 찾을 수 없습니다.\"}"
					)
				)
			),
		}
	)
	@DeleteMapping("/{id}/favorite")
	public ResponseEntity<Map<String, String>> cancelFavoriteGathering(@PathVariable("id") Long id,
		@Parameter(hidden = true) @Authorize User user) {

		gatheringService.cancelFavorite(id, user);
		return new ResponseEntity<>(Map.of("message", "모임 찜하기를 취소했습니다."), HttpStatus.OK);
	}

}
