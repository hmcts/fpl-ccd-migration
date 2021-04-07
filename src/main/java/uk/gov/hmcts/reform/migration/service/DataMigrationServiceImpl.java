package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {
    List<Long> caseIds = List.of(1602246223743823L, 1611588537917646L);

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseIds.contains(caseDetails.getId());
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return Map.<String, Object>of("migrationId", "FPLA-2947");
    }
}
