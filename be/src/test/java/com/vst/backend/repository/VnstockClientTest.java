package com.vst.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.vst.backend.dto.vnstock.VnstockOhlcvDto;
import com.vst.backend.exception.DataFetchException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Direct (unproxied) test: exercises HTTP call + JSON parsing + error translation only.
 * Retry/circuit-breaker behavior requires the Spring AOP proxy and is not covered here.
 */
class VnstockClientTest {

    @Test
    void parsesSuccessfulResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestToUriTemplate("http://fake/ohlcv?symbol={symbol}&start={start}&end={end}",
                        "VNM", "2026-08-01", "2026-09-03"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        [{"time":"2026-08-25T07:00:00","open":63.1,"high":63.8,"low":62.5,"close":62.6,"volume":3343900}]
                        """,
                        MediaType.APPLICATION_JSON));

        VnstockClient client = new VnstockClient(builder, "http://fake");
        List<VnstockOhlcvDto> result = client.fetchOhlcv("VNM", "2026-08-01", "2026-09-03");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).close()).isEqualByComparingTo("62.6");
        server.verify();
    }

    @Test
    void translatesHttpFailureToDataFetchException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestToUriTemplate("http://fake/ohlcv?symbol={symbol}&start={start}&end={end}",
                        "VNM", "2026-08-01", "2026-09-03"))
                .andRespond(withServerError());

        VnstockClient client = new VnstockClient(builder, "http://fake");

        assertThatThrownBy(() -> client.fetchOhlcv("VNM", "2026-08-01", "2026-09-03"))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("VNM");
    }
}
