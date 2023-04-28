package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.model.DfjAreaCourtMapping;
import uk.gov.hmcts.reform.migration.query.BooleanQuery;
import uk.gov.hmcts.reform.migration.query.ESQuery;
import uk.gov.hmcts.reform.migration.query.ExistsQuery;
import uk.gov.hmcts.reform.migration.query.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    public static final String DFJ_AREA = "dfjArea";
    private final DfjAreaLookUpService dfjAreaLookUpService;
    private final ObjectMapper objectMapper;
    private final Map<String, Function<Map<String, Object>, Map<String, Object>>> migrations = Map.of(
        "DFPL-1124", this::run1124,
        "DFPL-test", this::triggerOnlyMigration,
        "DFPL-1124Rollback", this::run1124Rollback
    );

    private final Map<String, ESQuery> queries = Map.of(
        "DFPL-1124", this.query1124(),
        "DFPL-test", this.query1124()
    );

    @Override
    public void validateMigrationId(String migrationId) {
        if (!migrations.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }
    }

    @Override
    public ESQuery getQuery(String migrationId) {
        if (!queries.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }
        return queries.get(migrationId);
    }

    public void validateQueryId(String migrationId) {
        if (!queries.containsKey(migrationId)) {
            throw new NoSuchElementException("No query mapped to " + migrationId);
        }
    }

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> true;
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data, String migrationId) {
        requireNonNull(migrationId);
        if (!migrations.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }

        // Perform Migration
        return migrations.get(migrationId).apply(data);
    }

    private Map<String, Object> run1124(Map<String, Object> data) {

        Long caseId = (Long) data.get(CASE_ID);
        Object court = data.get("court");

        Map<String, Object> updatedData = new HashMap<>();
        if (Objects.nonNull(court)) {
            Map<String, String> courtMap = objectMapper.convertValue(court, new TypeReference<>() {
            });
            String courtCode = courtMap.get("code");
            DfjAreaCourtMapping dfjArea = dfjAreaLookUpService.getDfjArea(courtCode);
            updatedData.put(DFJ_AREA, dfjArea.getDfjArea());
            updatedData.put(dfjArea.getCourtField(), courtCode);
            log.info("Migration {id = DFPL-1124, case reference = {}} updated dfj area and relevant court field",
                caseId);
            return updatedData;
        } else {
            log.warn("Migration {id = DFPL-1124, case reference = {}} doesn't have court info ",
                caseId);
        }
        return new HashMap<>();
    }

    private ESQuery query1124() {
        return BooleanQuery.builder()
            .filter(Filter.builder()
                .clauses(List.of(ExistsQuery.of("data.court")))
                .build())
            .build();
    }

    private Map<String, Object> run1124Rollback(Map<String, Object> data) {
        Long caseId = (Long) data.get(CASE_ID);
        if (Objects.nonNull(data.get(DFJ_AREA))) {
            log.info("Rollback initiated {id = DFPL-1124, case reference = {}}",
                caseId);
        } else {
            log.warn("Rollback ignored {id = DFPL-1124, case reference = {}}",
                caseId);
        }
        return new HashMap<>();
    }

    private Map<String, Object> triggerOnlyMigration(Map<String, Object> data) {
        // do nothing
        return new HashMap<>();
    }
}
