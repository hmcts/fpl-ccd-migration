package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {
    private static final String FAM_MAN_NUM = "SA20C50050";

    public Predicate<CaseDetails> accepts() {
        return caseDetails -> FAM_MAN_NUM.equals(caseDetails.getData().get("familyManCaseNumber"));
    }

    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", "FPLA-3126");
    }
}
