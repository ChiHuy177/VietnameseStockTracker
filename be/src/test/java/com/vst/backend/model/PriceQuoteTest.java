package com.vst.backend.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * FE phân biệt "chưa có dữ liệu" bằng field **vắng mặt** ({@code undefined}), không phải
 * {@code null} — gửi null lên là FE coi như có dữ liệu rồi format một giá trị không tồn tại (bảng
 * mua/bán rỗng chỉ có header, dòng room ngoại toàn dấu "—"). Test này giữ quyết định đó
 * ({@code @JsonInclude(NON_NULL)}) khỏi bị bỏ mất khi ai đó sửa model.
 */
class PriceQuoteTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private static PriceQuote quoteWithExtendedFields(List<PriceLevel> bids, Long foreignRoom) {
        return new PriceQuote(
                "VNM", Instant.ofEpochMilli(1L), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, 1L,
                null, null, null, bids, null, null, null, foreignRoom, null);
    }

    @Test
    void omitsMissingOptionalFieldsFromJson() throws Exception {
        String json = objectMapper.writeValueAsString(quoteWithExtendedFields(null, null));

        assertThat(json).contains("\"symbol\"").contains("\"price\"");
        assertThat(json)
                .doesNotContain("bids")
                .doesNotContain("asks")
                .doesNotContain("ceilingPrice")
                .doesNotContain("foreignRoom")
                .doesNotContain("totalValue");
    }

    @Test
    void keepsOptionalFieldsThatHaveAValue() throws Exception {
        String json = objectMapper.writeValueAsString(
                quoteWithExtendedFields(List.of(new PriceLevel(BigDecimal.valueOf(61_100), 12_300L)), 1_234L));

        assertThat(json).contains("\"bids\"").contains("61100").contains("12300");
        assertThat(json).contains("\"foreignRoom\":1234");
    }
}
