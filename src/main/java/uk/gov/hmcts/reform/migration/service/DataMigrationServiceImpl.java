package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.exception.CaseMigrationSkippedException;
import uk.gov.hmcts.reform.migration.query.BooleanQuery;
import uk.gov.hmcts.reform.migration.query.EsQuery;
import uk.gov.hmcts.reform.migration.query.ExistsQuery;
import uk.gov.hmcts.reform.migration.query.Filter;
import uk.gov.hmcts.reform.migration.query.MatchQuery;
import uk.gov.hmcts.reform.migration.query.Must;
import uk.gov.hmcts.reform.migration.query.MustNot;
import uk.gov.hmcts.reform.migration.query.TermQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    public static final String COURT = "court";
    private final ObjectMapper objectMapper;
    private final Map<String, Function<Map<String, Object>, Map<String, Object>>> migrations = Map.of(
        "DFPL-log", this::triggerOnlyMigration,
        "DFPL-2527", this::triggerOnlyMigration,
        "DFPL-2551", this::triggerOnlyMigration
        );

    private final Map<String, EsQuery> queries = Map.of(
        "DFPL-log", this.query1934(),
        "DFPL-1934", this.query1934(),
        "DFPL-1233", this.query1233(),
        "DFPL-1233Rollback", this.query1233(),
        "DFPL-AM", this.queryAM(),
        "DFPL-AM-Rollback", this.queryAM()
    );

    private EsQuery queryAM() {
        final MatchQuery openCases = MatchQuery.of("state", "Open");
        final MatchQuery deletedCases = MatchQuery.of("state", "Deleted");
        final MatchQuery returnedCases = MatchQuery.of("state", "RETURNED");
        final MatchQuery closedCases = MatchQuery.of("state", "CLOSED");

        return BooleanQuery.builder()
            .mustNot(MustNot.builder()
                .clauses(List.of(openCases, deletedCases, returnedCases, closedCases))
                .build())
            .build();
    }

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

    private EsQuery query1934() {
        // Can skip Open + Deleted cases as these won't have the field on them
        final MatchQuery openCases = MatchQuery.of("state", "Open");
        final MatchQuery deletedCases = MatchQuery.of("state", "Deleted");

        return BooleanQuery.builder()
            .mustNot(MustNot.builder()
                .clauses(List.of(openCases, deletedCases))
                .build())
            .build();
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

    private EsQuery query1855() {
        return BooleanQuery.builder()
            .filter(Filter.builder()
                .clauses(List.of(
                    BooleanQuery.builder()
                        .must(Must.of(ExistsQuery.of("data.court")))
                        .must(Must.of(TermQuery.of("data.court.code", "270")))
                        .build()
                ))
                .build())
            .build();
    }

    private EsQuery query1233() {
        return BooleanQuery.builder()
            .filter(Filter.builder()
                .clauses(List.of(
                    BooleanQuery.builder()
                        .must(Must.of(ExistsQuery.of("data.hearingDetails")))
                        .build()
                ))
                .build())
            .build();
    }

    private Map<String, Object> triggerOnlyMigration(Map<String, Object> data) {
        // do nothing
        return new HashMap<>();
    }

    private Map<String, Object> run1934(Map<String, Object> data) {
        // do nothing
        if (isEmpty(data.get("changeOrganisationRequestField"))) {
            throw new CaseMigrationSkippedException("Skipping case, changeOrganisationRequestField is empty");
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private boolean processHearingDetails(Object hearingDetails, Predicate<Map<String, Object>> checkHearingDetails) {
        if (Objects.nonNull(hearingDetails)) {
            List<Map<String, Object>> detailsMap = objectMapper.convertValue(hearingDetails, new TypeReference<>() {});
            return detailsMap.stream()
                .map(hearingDetail -> hearingDetail.get("value"))
                .filter(Objects::nonNull)
                .map(value -> (Map<String, Object>) value)
                .anyMatch(checkHearingDetails);
        }
        return false;
    }

    private Map<String, Object> run1233(Map<String, Object> data) throws CaseMigrationSkippedException {
        Object hearingDetails = data.get("hearingDetails");
        boolean hasOtherTypeHearings = processHearingDetails(hearingDetails, hearingDetail -> hearingDetail.get("type")
            .equals("OTHER"));

        Object cancelledHearingDetails = data.get("cancelledHearingDetails");
        boolean hasOtherTypeCancelledHearings = processHearingDetails(cancelledHearingDetails, hearingDetail ->
            "OTHER".equals(hearingDetail.get("type")));

        if (!hasOtherTypeHearings && !hasOtherTypeCancelledHearings) {
            throw new CaseMigrationSkippedException("Skipping case - no hearings with type OTHER found.");
        }
        return new HashMap<>();
    }

    private Map<String, Object> run1233Rollback(Map<String, Object> data) throws CaseMigrationSkippedException {
        Object hearingDetails = data.get("hearingDetails");
        boolean hasNonEmptyTypeDetails = processHearingDetails(hearingDetails, hearingDetail ->
            Objects.nonNull((hearingDetail.get("typeDetails"))));

        Object cancelledHearingDetails = data.get("cancelledHearingDetails");
        boolean hasNonEmptyTypeCancelledDetails = processHearingDetails(cancelledHearingDetails, hearingDetail ->
            Objects.nonNull(hearingDetail.get("typeDetails")));

        if (!hasNonEmptyTypeDetails && !hasNonEmptyTypeCancelledDetails) {
            throw new CaseMigrationSkippedException("Skipping case - no hearings with type details set.");
        }
        return new HashMap<>();
    }
}
