package uk.gov.hmcts.reform.migration.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public class DataMigrationServiceImpl implements DataMigrationService<Object>{

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .map(CaseDetails::getData)
            .filter(data -> "CF20C50014".equals(data.get("familyManCaseNumber")))
            .isPresent();
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return new HashMap<>();
    }
}
