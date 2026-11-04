package com.vst.backend.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vst.backend.service.OhlcvIngestionService;

@WebMvcTest(StockIngestionController.class)
@ActiveProfiles("dev")
class StockIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OhlcvIngestionService ohlcvIngestionService;

    @Test
    void postIngestTriggersIngestion() throws Exception {
        mockMvc.perform(post("/api/v1/stocks/ingest"))
                .andExpect(status().isNoContent());

        verify(ohlcvIngestionService).ingestAll();
    }
}

