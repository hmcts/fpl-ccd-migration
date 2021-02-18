package uk.gov.hmcts.reform.migration.service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>>{

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .map(CaseDetails::getData)
            .filter(this::shouldMigrate)
            .isPresent();
    }

    // some might have already been updated as users could have progressed through the events manually
    // but it won't hurt to migrate them
    private boolean shouldMigrate(Map<String, Object> data) {
        return data.containsKey("furtherEvidenceDocuments") || data.containsKey("furtherEvidenceDocumentsLA")
        || data.containsKey("correspondenceDocuments") || data.containsKey("correspondenceDocumentsLA")
        || data.containsKey("hearingFurtherEvidenceDocuments") || data.containsKey("c2DocumentBundle");
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", "FPLA-2722");
    }
}
