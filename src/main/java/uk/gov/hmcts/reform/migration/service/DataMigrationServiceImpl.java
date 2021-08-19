package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.Predicate;

@Component
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    private static final String MIGRATION_ID = "FPLA-3238";

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseData -> !caseData.getData().containsKey("localAuthorities");
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", MIGRATION_ID);
    }
}
