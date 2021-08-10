package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.Predicate;

@Component
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {
    @Override
    public Predicate<CaseDetails> accepts() {
        // Implement filter here that selects the cases to be migrated
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        // Populate a map here with data that wants to be present when connecting with the callback service
        throw new UnsupportedOperationException("not yet implemented");
    }
}
