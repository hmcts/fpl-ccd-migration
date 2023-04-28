package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.model.DfjAreaCourtMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    public static final String DFJ_AREA = "dfjArea";
    private final DfjAreaLookUpService dfjAreaLookUpService;
    private final ObjectMapper objectMapper;
    private final Map<String, Function<Map<String, Object>, Map<String, Object>>> migrations = Map.of(
        "DFPL-1124", this::run1124,
        "DFPL-1124Rollback", this::run1124Rollback
    );

    @Override
    public void validateMigrationId(String migrationId) {
        if (!migrations.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }
    }

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> true;
    }

    @Override
    public Map<String, Object> migrate(Map<String, Object> data) {
        String migrationId = null;
        try {
            migrationId = Optional.ofNullable(data.get(MIGRATION_ID_KEY))
                .map(String::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Migration id not set"));

            if (!migrations.containsKey(migrationId)) {
                throw new NoSuchElementException("No migration mapped to " + migrationId);
            }

            Long caseId = (Long) data.get(CASE_ID);

            log.info("Migration {id = {}, case reference = {}} started updating", migrationId, caseId);
            return migrations.get(migrationId).apply(data);
        } catch (NoSuchElementException noSuchElementException) {
            if (Objects.nonNull(migrationId)) {
                data.remove(MIGRATION_ID_KEY);
            }
            throw noSuchElementException;
        } finally {
            if (Objects.nonNull(migrationId)) {
                data.remove(CASE_ID);
            }
        }
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
        return Map.of();
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
}
