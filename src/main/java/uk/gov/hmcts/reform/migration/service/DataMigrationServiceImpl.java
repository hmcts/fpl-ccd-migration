package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    public static final List<String> INVALID_CASE_STATES = List.of("CLOSED", "Open", "Deleted");
    private final ObjectMapper mapper;

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .filter(details -> !INVALID_CASE_STATES.contains(details.getState()))
            .map(CaseDetails::getData)
            .filter(data -> !data.containsKey("noticeOfChangeAnswers0"))
            .filter(data -> !data.containsKey("respondentPolicy0"))
            .filter(this::filterRespondents)
            .filter(this::filterApplicants)
            .isPresent();
    }

    private boolean filterApplicants(Map<String, Object> data) {
        Object o = data.get("applicants");
        List<Object> applicants = new ArrayList<>();
        if (o != null) {
            applicants = mapper.convertValue(o, new TypeReference<List<Object>>(){});
        }

        return !applicants.isEmpty();
    }

    private boolean filterRespondents(Map<String, Object> data) {
        Object o = data.get("respondents1");
        List<Object> respondents = new ArrayList<>();
        if (o != null) {
            respondents = mapper.convertValue(o, new TypeReference<List<Object>>(){});
        }

        return !respondents.isEmpty() && respondents.size() <= 10;
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", "FPLA-2961");
    }
}
