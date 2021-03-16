package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.of(caseDetails)
            .map(CaseDetails::getData)
            .filter(data -> "WR20C5001".equals(data.get("familyManCaseNumber")))
            .isPresent();
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", "FPLA-2871");
    }
}
