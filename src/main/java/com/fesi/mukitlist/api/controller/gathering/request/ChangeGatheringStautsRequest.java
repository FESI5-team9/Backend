package com.fesi.mukitlist.api.controller.gathering.request;

import com.fesi.mukitlist.core.gathering.constant.GatheringStatus;

public record ChangeGatheringStautsRequest(
	GatheringStatus status
) {
}
