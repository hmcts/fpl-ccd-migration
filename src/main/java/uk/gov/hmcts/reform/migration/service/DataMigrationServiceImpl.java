package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {
    private static final List<String> MIGRATED_HEARING_TYPES = List.of(
        "EMERGENCY_PROTECTION_ORDER",
        "INTERIM_CARE_ORDER",
        "CASE_MANAGEMENT",
        "ACCELERATED_DISCHARGE_OF_CARE",
        "FURTHER_CASE_MANAGEMENT",
        "FACT_FINDING",
        "ISSUE_RESOLUTION",
        "JUDGMENT_AFTER_HEARING",
        "FINAL",
        "FAMILY_DRUG_ALCOHOL_COURT",
        "PLACEMENT_HEARING"
    );
    private static final String OTHER = "OTHER";
    private final ObjectMapper objectMapper;
    private final Map<String, Consumer<Map<String, Object>>> migrations = Map.of(
        "DFPL-1233", this::run1233,
        "DFPL-1233Rollback", this::run1233Rollback
    );

    private final Map<String, Supplier<Predicate<String>>> predicate = Map.of(
        "DFPL-1233", () -> OTHER::equals,
        "DFPL-1233Rollback", () -> MIGRATED_HEARING_TYPES::contains
    );

    @Override
    public void validateMigrationId(String migrationId) {
        if (!migrations.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }
    }

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> {
            Map<String, Object> data = caseDetails.getData();
            String migrationId = getMigrationId(data);
            boolean migrationRequired = isMigrationRequired(caseDetails.getData(), predicate.get(migrationId).get());
            if (Objects.nonNull(data.get(CASE_ID))) {
                data.remove(CASE_ID);
            }
            return migrationRequired;
        };
    }

    private String getMigrationId(Map<String, Object> data) {
        String migrationId = null;
        try {
            migrationId = Optional.ofNullable(data.get(MIGRATION_ID_KEY))
                .map(String::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Migration id not set"));

            if (!migrations.containsKey(migrationId)) {
                throw new NoSuchElementException("No migration mapped to " + migrationId);
            }
        } catch (NoSuchElementException noSuchElementException) {
            if (Objects.nonNull(migrationId)) {
                data.remove(MIGRATION_ID_KEY);
            }
            throw noSuchElementException;
        }
        return migrationId;
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        try {
            String migrationId = (String)data.get(MIGRATION_ID_KEY);
            migrations.get(migrationId).accept(data);
        } finally {
            if (Objects.nonNull(data.get(CASE_ID))) {
                data.remove(CASE_ID);
            }
        }
        return data;
    }

    private void run1233Rollback(Map<String, Object> data) {
        Long caseId = (Long) data.get(CASE_ID);
        log.info("Rollback {id = {}}, case reference = {}} updating hearing type",
            data.get(MIGRATION_ID_KEY),
            caseId);
    }

    private void run1233(Map<String, Object> data) {
        Long caseId = (Long) data.get(CASE_ID);
        log.info("Migration {id = {}}, case reference = {}} updating hearing type",
            data.get(MIGRATION_ID_KEY),
            caseId);
    }

    private boolean isMigrationRequired(Map<String, Object> data, Predicate<String> isTypePresent) {
        boolean isMigrationRequired = false;
        Long caseId = (Long) data.get(CASE_ID);
        Object hearings = data.get("hearingDetails");
        log.info("caseId {} : hearings : {}", caseId, hearings);
        if (Objects.nonNull(hearings)) {
            ArrayList<Map<String, Object>> hearingDetails = objectMapper.convertValue(hearings, new TypeReference<>() {
            });

            for (Map<String, Object> hearingDetail: hearingDetails) {
                @SuppressWarnings("unchecked")
                Map<String, Object> value = (Map<String, Object>)hearingDetail.get("value");

                if ((Objects.isNull(value.get("type"))
                    || isTypePresent.test((String)value.get("type"))) && Objects.nonNull(value.get("typeDetails"))) {
                    isMigrationRequired = true;
                    log.info("Migration required for {id = {}}, case reference = {}} updating hearing type",
                        data.get(MIGRATION_ID_KEY),
                        caseId);
                }
            }
        }
        return isMigrationRequired;
    }
}
