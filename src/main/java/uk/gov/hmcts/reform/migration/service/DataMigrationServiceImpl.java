package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {

    public static final List<String> INVALID_CASE_STATES = List.of("CLOSED", "Open", "Deleted");

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .filter(caseDetailsMap -> !INVALID_CASE_STATES.contains(caseDetailsMap.getState()))
            .filter(caseDetailsMap -> !caseDetailsMap.getData().containsKey("noticeOfChangeAnswers0"))
            .filter(caseDetailsMap -> !caseDetailsMap.getData().containsKey("respondentPolicy0"))
            .isPresent();
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return Map.<String, Object>of("migrationId", "FPLA-2961");
    }
}
