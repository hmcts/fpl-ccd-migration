package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails.getData()
            .getOrDefault("familyManCaseNumber", "")
            .equals("PE21C50004");
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return Map.<String, Object>of("migrationId", "FPLA-3262");
    }
}
