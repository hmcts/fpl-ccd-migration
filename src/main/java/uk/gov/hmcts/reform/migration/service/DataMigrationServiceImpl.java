package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.exception.CaseMigrationSkippedException;
import uk.gov.hmcts.reform.domain.model.DfjAreaCourtMapping;
import uk.gov.hmcts.reform.migration.query.BooleanQuery;
import uk.gov.hmcts.reform.migration.query.EsQuery;
import uk.gov.hmcts.reform.migration.query.ExistsQuery;
import uk.gov.hmcts.reform.migration.query.Filter;
import uk.gov.hmcts.reform.migration.query.Must;
import uk.gov.hmcts.reform.migration.query.MustNot;

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
    public static final String COURT = "court";
    private final DfjAreaLookUpService dfjAreaLookUpService;
    private final ObjectMapper objectMapper;
    private final Map<String, Function<Map<String, Object>, Map<String, Object>>> migrations = Map.of(
        "DFPL-1124", this::run1124,
        "DFPL-log", this::triggerOnlyMigration,
        "DFPL-1124Rollback", this::run1124Rollback,
        "DFPL-GSWA", this::triggerOnlyMigration,
        "DFPL-1352", this::triggerOnlyMigration
    );

    private final Map<String, EsQuery> queries = Map.of(
        "DFPL-1124", this.query1124(),
        "DFPL-1124Rollback", this.topLevelFieldExistsQuery(DFJ_AREA),
        "DFPL-log", this.topLevelFieldExistsQuery(COURT)
    );

    @Override
    public void validateMigrationId(String migrationId) {
        if (!migrations.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }
    }

    @Override
    public EsQuery getQuery(String migrationId) {
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
        requireNonNull(migrationId, "Migration ID must not be null");
        if (!migrations.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }

        // Perform Migration
        return migrations.get(migrationId).apply(data);
    }

    private Map<String, Object> run1124(Map<String, Object> data) {

        Long caseId = (Long) data.get(CASE_ID);
        Object court = data.get(COURT);

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
            throw new CaseMigrationSkippedException("No `court` on the case");
        }
    }

    private EsQuery topLevelFieldExistsQuery(String field) {
        return BooleanQuery.builder()
            .filter(Filter.builder()
                .clauses(List.of(ExistsQuery.of("data." + field)))
                .build())
            .build();
    }

    /**
     * Fetch cases that have a court field AND do not have a dfjArea field (these have been migrated already).
     * @return EsQuery performing this search
     */
    private EsQuery query1124() {
        return BooleanQuery.builder()
            .filter(Filter.builder()
                .clauses(List.of(
                    BooleanQuery.builder()
                        .must(Must.of(ExistsQuery.of("data.court")))
                        .mustNot(MustNot.of(ExistsQuery.of("data.dfjArea")))
                        .build()
                ))
                .build())
            .build();
    }

    private Map<String, Object> run1124Rollback(Map<String, Object> data) {
        Long caseId = (Long) data.get(CASE_ID);
        if (Objects.nonNull(data.get(DFJ_AREA))) {
            log.info("Rollback initiated {id = DFPL-1124, case reference = {}}",
                caseId);
            return new HashMap<>();
        } else {
            throw new CaseMigrationSkippedException("No `dfjArea` on the case");
        }
    }

    private Map<String, Object> triggerOnlyMigration(Map<String, Object> data) {
        // do nothing
        return new HashMap<>();
    }
}
