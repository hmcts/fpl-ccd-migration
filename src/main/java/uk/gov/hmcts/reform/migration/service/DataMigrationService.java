package uk.gov.hmcts.reform.migration.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.Predicate;

public interface DataMigrationService<T> {
    String MIGRATION_ID_KEY = "migrationId";
    String CASE_ID = "id";

    Predicate<CaseDetails> accepts();

    T migrate(Map<String, Object> data);
    void validateMigrationId(String migrationId);
}
