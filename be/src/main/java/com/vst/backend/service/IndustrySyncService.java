package com.vst.backend.service;

import com.vst.backend.dto.vnstock.VnstockIndustryDto;
import com.vst.backend.mapper.IndustryMapper;
import com.vst.backend.model.StockIndustry;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.VnstockClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Sibling of {@link StockListingSyncService}, scoped to ICB sector classification instead of
 * name/exchange — kept as its own job because it needs a different vnstock source ("vci";
 * {@code Listing()}'s default doesn't implement ICB) and changes on the same rare cadence
 * (industry reassignments are not a daily event).
 *
 * <p>vnstock returns all 4 ICB levels (broad to narrow) per symbol in one response; this codebase
 * only uses level 2 (~19 sectors in the VN market, e.g. "Ngân hàng", "Bất động sản") — narrow
 * enough for a useful treemap, broad enough to match the sector list the FE mockup was designed
 * around.
 */
@Service
public class IndustrySyncService {

    private static final Logger log = LoggerFactory.getLogger(IndustrySyncService.class);

    private static final int ICB_LEVEL = 2;

    private final VnstockClient vnstockClient;
    private final IndustryMapper mapper;
    private final StockRepository stockRepository;

    public IndustrySyncService(VnstockClient vnstockClient, IndustryMapper mapper, StockRepository stockRepository) {
        this.vnstockClient = vnstockClient;
        this.mapper = mapper;
        this.stockRepository = stockRepository;
    }

    @Scheduled(cron = "${industry-sync.cron}")
    public void syncAll() {
        try {
            List<VnstockIndustryDto> dtos = vnstockClient.fetchIndustries();
            List<StockIndustry> industries = dtos.stream()
                    .filter(dto -> dto.icbLevel() != null && dto.icbLevel() == ICB_LEVEL)
                    .map(mapper::toDomain)
                    .toList();
            stockRepository.upsertIndustries(industries);
            log.info("Synced ICB sector metadata for {} symbols", industries.size());
        } catch (Exception e) {
            log.error("Industry sync failed", e);
        }
    }
}
