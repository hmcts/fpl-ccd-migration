package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {
    private static final long ID = 1618497329043582L;

    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .filter(details -> ID == details.getId())
            .isPresent();
    }

    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", "FPLA-3087");
    }
}
