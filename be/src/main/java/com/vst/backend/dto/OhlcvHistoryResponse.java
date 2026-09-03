package com.vst.backend.dto;

import java.util.List;

public record OhlcvHistoryResponse(String symbol, List<OhlcvBarResponse> bars) {
}
