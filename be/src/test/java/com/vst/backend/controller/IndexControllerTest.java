package com.vst.backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vst.backend.dto.IndexQuoteResponse;
import com.vst.backend.dto.OhlcvBarResponse;
import com.vst.backend.dto.OhlcvHistoryResponse;
import com.vst.backend.service.IndexQuoteService;
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

@WebMvcTest(IndexController.class)
class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IndexQuoteService indexQuoteService;

    @MockBean
    private OhlcvQueryService ohlcvQueryService;

    @Test
    void returnsCurrentIndexQuotes() throws Exception {
        when(indexQuoteService.getCurrentQuotes()).thenReturn(List.of(
                new IndexQuoteResponse("VNINDEX", "VN-INDEX", BigDecimal.valueOf(1735.09),
                        BigDecimal.valueOf(35.09), BigDecimal.valueOf(2.06), Instant.parse("2026-10-09T00:00:00Z"))));

        mockMvc.perform(get("/api/v1/indices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol", is("VNINDEX")))
                .andExpect(jsonPath("$[0].name", is("VN-INDEX")))
                .andExpect(jsonPath("$[0].value", is(1735.09)))
                .andExpect(jsonPath("$[0].percentChange", is(2.06)));
    }

    @Test
    void returnsIndexOhlcvHistory() throws Exception {
        OhlcvBarResponse bar = new OhlcvBarResponse(
                Instant.parse("2026-09-03T00:00:00Z"),
                BigDecimal.valueOf(1821.82), BigDecimal.valueOf(1829.84),
                BigDecimal.valueOf(1802.11), BigDecimal.valueOf(1827.72), 520_015_604L);
        when(ohlcvQueryService.getHistory("VNINDEX", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 11)))
                .thenReturn(new OhlcvHistoryResponse("VNINDEX", List.of(bar)));

        mockMvc.perform(get("/api/v1/indices/VNINDEX/ohlcv?start=2026-09-01&end=2026-10-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("VNINDEX")))
                .andExpect(jsonPath("$.bars[0].close", is(1827.72)));
    }
}
