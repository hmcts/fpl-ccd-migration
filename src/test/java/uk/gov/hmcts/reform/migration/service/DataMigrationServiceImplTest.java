package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.migration.service.DataMigrationService.MIGRATION_ID_KEY;


@ExtendWith(MockitoExtension.class)
class DataMigrationServiceImplTest {
    private DataMigrationServiceImpl dataMigrationService;

    @BeforeEach
    void setUp() {
        dataMigrationService = new DataMigrationServiceImpl(
            new ObjectMapper());
    }

    @Test
    void shouldThrowExceptionWhenMigrationKeyIsNotSet() {
        assertThatThrownBy(() -> dataMigrationService.accepts()
            .test(CaseDetails.builder().data(Map.of()).build()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Migration id not set");
    }

    @Test
    void shouldThrowExceptionWhenMigrationKeyIsInvalid() {
        Map<String, Object> data = new HashMap<>();
        data.put(MIGRATION_ID_KEY, "UNKNOW-123");
        assertThatThrownBy(() -> dataMigrationService.accepts()
            .test(CaseDetails.builder().data(data).build()))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to UNKNOW-123");
        assertThat(data.get(MIGRATION_ID_KEY)).isNull();
    }

    @Test
    void shouldReturnMigrationRequiredWhenOtherIsHearingType() {
        Map<String, String> value = Map.of(
            "type", "OTHER",
            "typeDetails", "EPO"
        );
        Map<String, Object> hearingDetail = Map.of(
            "value", value
        );
        List<Map<String, Object>> hearingDetails = List.of(hearingDetail);
        Map<String, Object> data = Map.of(
            MIGRATION_ID_KEY, "DFPL-1233",
            "hearingDetails", hearingDetails
        );
        boolean isMigrationRequired = dataMigrationService.accepts()
            .test(CaseDetails.builder()
                .data(data)
                .build());
        assertThat(isMigrationRequired).isTrue();
    }

    @Test
    void shouldReturnMigrationRequiredWhenEmergencyProtectionOrderWithHearingDetails() {
        Map<String, String> value = Map.of(
            "type", "EMERGENCY_PROTECTION_ORDER",
            "typeDetails", "EPO"
        );
        Map<String, Object> hearingDetail = Map.of(
            "value", value
        );
        List<Map<String, Object>> hearingDetails = List.of(hearingDetail);
        Map<String, Object> data = Map.of(
            MIGRATION_ID_KEY, "DFPL-1233Rollback",
            "hearingDetails", hearingDetails
        );
        boolean isMigrationRequired = dataMigrationService.accepts()
            .test(CaseDetails.builder()
                .data(data)
                .build());
        assertThat(isMigrationRequired).isTrue();
    }

    @Test
    void shouldReturnMigrationNotRequiredWhenCaseManagementHearingTypeHearingDetailsNotSet() {
        Map<String, String> value = Map.of(
            "type", "CASE_MANAGEMENT"
        );
        Map<String, Object> hearingDetail = Map.of(
            "value", value
        );
        List<Map<String, Object>> hearingDetails = List.of(hearingDetail);
        Map<String, Object> data = Map.of(
            MIGRATION_ID_KEY, "DFPL-1233",
            "hearingDetails", hearingDetails
        );
        boolean isMigrationRequired = dataMigrationService.accepts()
            .test(CaseDetails.builder()
                .data(data)
                .build());
        assertThat(isMigrationRequired).isFalse();
    }

    @Test
    void shouldReturnMigrationNotRequiredWhenEmergencyProtectionOrderWithHearingDetailsForMigration() {
        Map<String, String> value = Map.of(
            "type", "EMERGENCY_PROTECTION_ORDER",
            "typeDetails", "EPO"
        );
        Map<String, Object> hearingDetail = Map.of(
            "value", value
        );
        List<Map<String, Object>> hearingDetails = List.of(hearingDetail);
        Map<String, Object> data = Map.of(
            MIGRATION_ID_KEY, "DFPL-1233",
            "hearingDetails", hearingDetails
        );
        boolean isMigrationRequired = dataMigrationService.accepts()
            .test(CaseDetails.builder()
                .data(data)
                .build());
        assertThat(isMigrationRequired).isFalse();
    }

    @Test
    void shouldReturnMigrationIsRequiredWhenMigrationTypeIsNullAndTypeDetailsPresent() {

        Map<String, String> value = new HashMap<>();
        value.put("type", null);
        value.put("typeDetails", "EPO");

        Map<String, Object> hearingDetail = Map.of(
            "value", value
        );

        List<Map<String, Object>> hearingDetails = List.of(hearingDetail);
        Map<String, Object> data = Map.of(
            MIGRATION_ID_KEY, "DFPL-1233Rollback",
            "hearingDetails", hearingDetails
        );
        boolean isMigrationRequired = dataMigrationService.accepts()
            .test(CaseDetails.builder()
                .data(data)
                .build());
        assertThat(isMigrationRequired).isTrue();
    }
}
