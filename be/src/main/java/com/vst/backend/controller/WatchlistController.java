package com.vst.backend.controller;

import com.vst.backend.dto.AddWatchlistItemRequest;
import com.vst.backend.dto.WatchlistItemResponse;
import com.vst.backend.service.WatchlistService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** A single, global watchlist — this project has no user/auth concept yet. */
@RestController
@RequestMapping("/api/v1/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public List<WatchlistItemResponse> list() {
        return watchlistService.list().stream().map(WatchlistItemResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistItemResponse add(@Valid @RequestBody AddWatchlistItemRequest request) {
        return WatchlistItemResponse.from(watchlistService.add(request.symbol()));
    }

    @DeleteMapping("/{symbol}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String symbol) {
        watchlistService.remove(symbol);
    }
}
