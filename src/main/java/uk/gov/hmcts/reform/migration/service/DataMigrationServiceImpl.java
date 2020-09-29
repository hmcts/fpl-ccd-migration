package uk.gov.hmcts.reform.migration.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class DataMigrationServiceImpl implements DataMigrationService<Object> {
    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .map(CaseDetails::getData)
            .filter(data -> data.getOrDefault("familyManCaseNumber", "").equals("SA20C50002"))
            .isPresent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object migrate(Map<String, Object> data) {
        return data;
    }
}
