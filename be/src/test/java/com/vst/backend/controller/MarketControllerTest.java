package com.vst.backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vst.backend.dto.MarketBreadthResponse;
import com.vst.backend.dto.SectorSnapshotResponse;
import com.vst.backend.service.MarketBreadthService;
import com.vst.backend.service.SectorAnalyticsService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MarketController.class)
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarketBreadthService marketBreadthService;

    @MockBean
    private SectorAnalyticsService sectorAnalyticsService;

    @Test
    void returnsMarketBreadth() throws Exception {
        when(marketBreadthService.getBreadth()).thenReturn(
                new MarketBreadthResponse(296, 330, 213, 18, 7, Instant.parse("2026-10-09T00:00:00Z")));

        mockMvc.perform(get("/api/v1/market/breadth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.advancing", is(296)))
                .andExpect(jsonPath("$.declining", is(330)))
                .andExpect(jsonPath("$.ceiling", is(18)));
    }

    @Test
    void returnsSectorSnapshots() throws Exception {
        when(sectorAnalyticsService.getSectorSnapshots()).thenReturn(List.of(
                new SectorSnapshotResponse("8300", "Ngân hàng", BigDecimal.valueOf(24.5),
                        BigDecimal.valueOf(1.23), BigDecimal.valueOf(842))));

        mockMvc.perform(get("/api/v1/market/sectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("8300")))
                .andExpect(jsonPath("$[0].marketCapWeight", is(24.5)))
                .andExpect(jsonPath("$[0].moneyFlow", is(842)));
    }
}
