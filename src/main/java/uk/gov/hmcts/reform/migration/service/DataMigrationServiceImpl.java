package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {
    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .map(CaseDetails::getData)
            .filter(data -> data.getOrDefault("familyManCaseNumber", "").equals("ZW21C50002"))
            .isPresent();
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return Map.<String, Object>of("migrationId", "FPLA-2740");
    }
}
