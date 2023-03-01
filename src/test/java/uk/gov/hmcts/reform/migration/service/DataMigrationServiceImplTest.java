package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.model.DfjAreaCourtMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.migration.service.DataMigrationService.MIGRATION_ID_KEY;
import static uk.gov.hmcts.reform.migration.service.DataMigrationServiceImpl.DFJ_AREA;


@ExtendWith(MockitoExtension.class)
class DataMigrationServiceImplTest {
    @Mock
    private DfjAreaLookUpService dfjAreaLookUpService;
    private DataMigrationServiceImpl dataMigrationService;

    CaseDetails caseDetails;
    DfjAreaCourtMapping dfjAreaCourtMapping;

    @BeforeEach
    void setUp() {
        dataMigrationService = new DataMigrationServiceImpl(dfjAreaLookUpService,
            new ObjectMapper());

        Map<String, String> court = Map.of("code", "344",
            "name", "Family Court sitting at Swansea",
            "email", "FamilyPublicLaw+sa@gmail.com"
        );

        caseDetails = CaseDetails.builder()
            .data(Map.of("court", court))
            .build();

        dfjAreaCourtMapping = DfjAreaCourtMapping.builder()
            .courtCode(court.get("code"))
            .dfjArea("SWANSEA")
            .courtField("swanseaDFJCourt")
            .build();
    }

    @Test
    void shouldReturnTrueWhenCourtPresent() {
        assertThat(dataMigrationService.accepts().test(caseDetails)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCourtPresent() {
        assertThat(dataMigrationService.accepts().test(CaseDetails
            .builder()
            .data(Map.of())
            .build())).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenMigrationKeyIsNotSet() {
        assertThatThrownBy(() -> dataMigrationService.migrate(Map.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Migration id not set");
    }

    @Test
    void shouldThrowExceptionWhenMigrationKeyIsInvalid() {
        Map<String, Object> data = new HashMap<>();
        data.put(MIGRATION_ID_KEY, "UNKNOW-123");
        assertThatThrownBy(() -> dataMigrationService.migrate(data))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to UNKNOW-123");
        assertThat(data.get(MIGRATION_ID_KEY)).isNull();
    }

    @Test
    void shouldSetDFJAreaAndCourtFieldWhenMigrationIdIsValid() {
        when(dfjAreaLookUpService.getDfjArea("344"))
            .thenReturn(dfjAreaCourtMapping);

        Map<String, Object> data = new HashMap<>();
        data.put("id", 1649150882331141L);
        data.put(MIGRATION_ID_KEY, "DFPL-1124");
        data.put("court", caseDetails.getData().get("court"));

        Map<String, Object> updatedData = dataMigrationService.migrate(data);
        assertThat(updatedData.get(DFJ_AREA))
            .isEqualTo(dfjAreaCourtMapping.getDfjArea());
        assertThat(updatedData.get(dfjAreaCourtMapping.getCourtField()))
            .isEqualTo(dfjAreaCourtMapping.getCourtCode());
        assertThat(data.get(MIGRATION_ID_KEY)).isEqualTo("DFPL-1124");
    }

    @Test
    void shouldIgnoreRollbackWhenDFJAreaNotPresent() {

        Map<String, Object> data = new HashMap<>();
        data.put("id", 1649150882331141L);
        data.put(MIGRATION_ID_KEY, "DFPL-1124Rollback");

        Map<String, Object> updatedData = dataMigrationService.migrate(data);
        assertThat(updatedData.get(DFJ_AREA))
            .isNull();
        assertThat(updatedData.get(dfjAreaCourtMapping.getCourtField()))
            .isNull();
        assertThat(data.get(MIGRATION_ID_KEY)).isEqualTo("DFPL-1124Rollback");
    }

    @Test
    void shouldNotSetDFJAreaAndCourtFieldWhenMigrationIdIsValidButCourtNotPresent() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1649150882331141L);
        data.put(MIGRATION_ID_KEY, "DFPL-1124");

        Map<String, Object> updatedData = dataMigrationService.migrate(data);
        assertThat(updatedData.get(DFJ_AREA))
            .isNull();
        assertThat(updatedData.get(dfjAreaCourtMapping.getCourtField()))
            .isNull();
        assertThat(data.get(MIGRATION_ID_KEY)).isEqualTo("DFPL-1124");
    }


}
