package com.vst.backend.repository;

import com.vst.backend.dto.vnstock.VnstockListingItemDto;
import com.vst.backend.dto.vnstock.VnstockOhlcvDto;
import com.vst.backend.dto.vnstock.VnstockPriceBoardDto;
import com.vst.backend.exception.DataFetchException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** HTTP client for the Python vnstock microservice ({@code python/src/vst_python/api.py}). */
@Repository
public class VnstockClient {

    private final RestClient restClient;

    public VnstockClient(RestClient.Builder builder, @Value("${vnstock.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Retry(name = "vnstock")
    @CircuitBreaker(name = "vnstock")
    public List<VnstockOhlcvDto> fetchOhlcv(String symbol, String start, String end) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/ohlcv")
                            .queryParam("symbol", symbol)
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<VnstockOhlcvDto>>() {
                    });
        } catch (RestClientException e) {
            throw new DataFetchException("Failed to fetch OHLCV for " + symbol + " from vnstock service", e);
        }
    }

    @Retry(name = "vnstock")
    @CircuitBreaker(name = "vnstock")
    public List<VnstockListingItemDto> fetchListing() {
        try {
            return restClient.get()
                    .uri("/listing")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<VnstockListingItemDto>>() {
                    });
        } catch (RestClientException e) {
            throw new DataFetchException("Failed to fetch stock listing from vnstock service", e);
        }
    }

    @Retry(name = "vnstock")
    @CircuitBreaker(name = "vnstock")
    public List<VnstockPriceBoardDto> fetchPriceBoard(List<String> symbols) {
        String symbolsCsv = String.join(",", symbols);
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/price-board")
                            .queryParam("symbols", symbolsCsv)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<VnstockPriceBoardDto>>() {
                    });
        } catch (RestClientException e) {
            throw new DataFetchException("Failed to fetch price board from vnstock service", e);
        }
    }
}
