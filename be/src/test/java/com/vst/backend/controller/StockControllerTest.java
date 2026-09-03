package com.vst.backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vst.backend.dto.OhlcvBarResponse;
import com.vst.backend.dto.OhlcvHistoryResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.service.OhlcvQueryService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OhlcvQueryService ohlcvQueryService;

    @Test
    void returnsOhlcvHistory() throws Exception {
        OhlcvBarResponse bar = new OhlcvBarResponse(
                Instant.parse("2026-08-25T00:00:00Z"),
                BigDecimal.valueOf(63.1), BigDecimal.valueOf(63.8),
                BigDecimal.valueOf(62.5), BigDecimal.valueOf(62.6), 3_343_900L);
        when(ohlcvQueryService.getHistory("VNM", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 3)))
                .thenReturn(new OhlcvHistoryResponse("VNM", List.of(bar)));

        mockMvc.perform(get("/api/v1/stocks/VNM/ohlcv?start=2026-08-01&end=2026-09-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("VNM")))
                .andExpect(jsonPath("$.bars[0].close", is(62.6)));
    }

    @Test
    void returns404ForUnknownSymbol() throws Exception {
        when(ohlcvQueryService.getHistory("ZZZZ", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 3)))
                .thenThrow(new ResourceNotFoundException("Stock", "ZZZZ"));

        mockMvc.perform(get("/api/v1/stocks/ZZZZ/ohlcv?start=2026-08-01&end=2026-09-03"))
                .andExpect(status().isNotFound());
    }
}
