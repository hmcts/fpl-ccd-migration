package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {
    Long caseId = 1616408548410918L;

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseId.equals(caseDetails.getId());
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", "FPLA-3037");
    }
}
