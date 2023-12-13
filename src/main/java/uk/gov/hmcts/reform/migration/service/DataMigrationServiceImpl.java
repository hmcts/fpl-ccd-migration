package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.migration.query.BooleanQuery;
import uk.gov.hmcts.reform.migration.query.EsQuery;
import uk.gov.hmcts.reform.migration.query.ExistsQuery;
import uk.gov.hmcts.reform.migration.query.Filter;
import uk.gov.hmcts.reform.migration.query.MustNot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    public static final String COURT = "court";
    private final ObjectMapper objectMapper;
    private final Map<String, Function<Map<String, Object>, Map<String, Object>>> migrations = Map.of(
        "DFPL-log", this::triggerOnlyMigration,
        "DFPL-CFV", this::triggerOnlyMigration,
        "DFPL-CFV-Rollback", this::triggerOnlyMigration,
        "DFPL-CFV-Failure", this::triggerOnlyMigration,
        "DFPL-CFV-dry", this::triggerOnlyMigration
    );

    private final Map<String, EsQuery> queries = Map.of(
        "DFPL-log", this.topLevelFieldExistsQuery(COURT),
        "DFPL-CFV", this.topLevelFieldDoesNotExistQuery("hasBeenCFVMigrated"),
        "DFPL-CFV-Rollback", this.topLevelFieldExistsQuery("hasBeenCFVMigrated"),
        "DFPL-CFV-Failure", this.topLevelFieldDoesNotExistQuery("hasBeenCFVMigrated"),
        "DFPL-CFV-dry", this.topLevelFieldDoesNotExistQuery("hasBeenCFVMigrated")
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
        log.info(queries.get(migrationId).toQueryContext(100, 0).toString());
        return queries.get(migrationId);
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

    private EsQuery topLevelFieldExistsQuery(String field) {
        return BooleanQuery.builder()
            .filter(Filter.builder()
                .clauses(List.of(ExistsQuery.of("data." + field)))
                .build())
            .build();
    }

    private EsQuery topLevelFieldDoesNotExistQuery(String field) {
        return BooleanQuery.builder()
            .filter(Filter.builder()
                .clauses(List.of(BooleanQuery.builder()
                    .mustNot(MustNot.of(ExistsQuery.of("data." + field)))
                    .build()))
                .build())
            .build();
    }

    private Map<String, Object> triggerOnlyMigration(Map<String, Object> data) {
        // do nothing
        return new HashMap<>();
    }
}
