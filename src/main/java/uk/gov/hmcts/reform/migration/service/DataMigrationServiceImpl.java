package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Component
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {
    private static final List<String> IGNORED_STATES = List.of("Open", "RETURNED");
    private static final int MAX_CHILDREN = 15;

    private final ObjectMapper mapper;

    @Autowired
    public DataMigrationServiceImpl(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.of(caseDetails)
            .filter(details -> !IGNORED_STATES.contains(details.getState()))
            .filter(this::hasValidNumberOfChildren)
            .filter(this::hasNotAlreadyGotDetails)
            .isPresent();
    }

    private boolean hasValidNumberOfChildren(CaseDetails details) {
        Object rawChildren = details.getData().get("children1");

        if (null == rawChildren) {
            return false;
        }

        List<?> children = mapper.convertValue(rawChildren, new TypeReference<>() {});
        return MAX_CHILDREN >= children.size();
    }

    private boolean hasNotAlreadyGotDetails(CaseDetails details) {
        Map<String, Object> data = details.getData();
        return !data.containsKey("childPolicy0") && !data.containsKey("noticeOfChangeChildAnswers0");
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        return Map.of("migrationId", "FPLA-3132");
    }
}
