package com.vst.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AddWatchlistItemRequest(@NotBlank String symbol) {
}
