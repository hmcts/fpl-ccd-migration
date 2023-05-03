package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

    private static final String INVALID_MIGRATION_ID = "NOT_A_MIGRATION";

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
    void shouldThrowExceptionWhenMigrationKeyIsNotSet() {
        assertThatThrownBy(() -> dataMigrationService.migrate(Map.of(), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Migration ID must not be null");
    }

    @Test
    void shouldThrowExceptionWhenMigrationKeyIsInvalid() {
        Map<String, Object> data = new HashMap<>();
        assertThatThrownBy(() -> dataMigrationService.migrate(data, INVALID_MIGRATION_ID))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
        assertThat(data.get(MIGRATION_ID_KEY)).isNull();
    }

    @Nested
    class Dfpl1124 {

        private static final String MIGRATION_ID = "DFPL-1124";
        private static final String ROLLBACK_MIGRATION_ID = "DFPL-1124Rollback";

        @Test
        void shouldSetDfjAreaAndCourtFieldWhenMigrationIdIsValid() {
            when(dfjAreaLookUpService.getDfjArea("344"))
                .thenReturn(dfjAreaCourtMapping);

            Map<String, Object> data = new HashMap<>();
            data.put("court", caseDetails.getData().get("court"));

            Map<String, Object> updatedData = dataMigrationService.migrate(data, MIGRATION_ID);
            assertThat(updatedData.get(DFJ_AREA))
                .isEqualTo(dfjAreaCourtMapping.getDfjArea());
            assertThat(updatedData.get(dfjAreaCourtMapping.getCourtField()))
                .isEqualTo(dfjAreaCourtMapping.getCourtCode());
        }

        @Test
        void shouldIgnoreRollbackWhenDfjAreaNotPresent() {

            Map<String, Object> data = new HashMap<>();

            Map<String, Object> updatedData = dataMigrationService.migrate(data, ROLLBACK_MIGRATION_ID);
            assertThat(updatedData.get(DFJ_AREA))
                .isNull();
            assertThat(updatedData.get(dfjAreaCourtMapping.getCourtField()))
                .isNull();
        }

        @Test
        void shouldNotSetDfjAreaAndCourtFieldWhenMigrationIdIsValidButCourtNotPresent() {
            Map<String, Object> data = new HashMap<>();

            Map<String, Object> updatedData = dataMigrationService.migrate(data, MIGRATION_ID);
            assertThat(updatedData.get(DFJ_AREA))
                .isNull();
            assertThat(updatedData.get(dfjAreaCourtMapping.getCourtField()))
                .isNull();
        }

    }

}
