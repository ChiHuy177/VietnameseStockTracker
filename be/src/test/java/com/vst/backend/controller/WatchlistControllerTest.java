package com.vst.backend.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.WatchlistEntry;
import com.vst.backend.service.WatchlistService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WatchlistController.class)
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WatchlistService watchlistService;

    @Test
    void listsWatchlist() throws Exception {
        when(watchlistService.list()).thenReturn(
                List.of(new WatchlistEntry("VNM", "CTCP Sữa Việt Nam", "HOSE", Instant.parse("2026-09-03T00:00:00Z"))));

        mockMvc.perform(get("/api/v1/watchlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol", is("VNM")));
    }

    @Test
    void addsToWatchlist() throws Exception {
        when(watchlistService.add("VNM")).thenReturn(
                new WatchlistEntry("VNM", "CTCP Sữa Việt Nam", "HOSE", Instant.parse("2026-09-03T00:00:00Z")));

        mockMvc.perform(post("/api/v1/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Body("VNM"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol", is("VNM")));
    }

    @Test
    void rejectsBlankSymbolOnAdd() throws Exception {
        mockMvc.perform(post("/api/v1/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Body(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removesFromWatchlist() throws Exception {
        mockMvc.perform(delete("/api/v1/watchlist/VNM"))
                .andExpect(status().isNoContent());
    }

    @Test
    void returns404WhenRemovingUnknownSymbol() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Stock", "ZZZZ"))
                .when(watchlistService).remove("ZZZZ");

        mockMvc.perform(delete("/api/v1/watchlist/ZZZZ"))
                .andExpect(status().isNotFound());
    }

    private record Body(String symbol) {
    }
}
