package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {
    private static final String FAMILY_MAN_ID = "CF21C50022";

    public Predicate<CaseDetails> accepts() {
        return caseDetails -> FAMILY_MAN_ID.equals(caseDetails.getData().get("familyManCaseNumber"));
    }

    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", "FPLA-3088");
    }
}
