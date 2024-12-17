package uk.gov.hmcts.reform.migration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.migration.service.DataMigrationService.MIGRATION_ID_KEY;


@ExtendWith(MockitoExtension.class)
class DataMigrationServiceImplTest {

    private static final String INVALID_MIGRATION_ID = "NOT_A_MIGRATION";

    private DataMigrationServiceImpl dataMigrationService;

    CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        dataMigrationService = new DataMigrationServiceImpl();

        Map<String, String> court = Map.of("code", "344",
            "name", "Family Court sitting at Swansea",
            "email", "FamilyPublicLaw+sa@gmail.com"
        );

        caseDetails = CaseDetails.builder()
            .data(Map.of("court", court))
            .build();
    }

    @Test
    void shouldReturnTrueWhenCourtPresent() {
        assertThat(dataMigrationService.accepts().test(caseDetails)).isTrue();
    }


    @Test
    void shouldThrowExceptionWhenMigrationKeyIsNotSet() {
        assertThatThrownBy(() -> dataMigrationService.migrate(caseDetails, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Migration ID must not be null");
    }

    @Test
    void shouldThrowExceptionWhenMigrationKeyIsInvalid() {
        Map<String, Object> data = new HashMap<>();
        assertThatThrownBy(() -> dataMigrationService.migrate(caseDetails, INVALID_MIGRATION_ID))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
        assertThat(data.get(MIGRATION_ID_KEY)).isNull();
    }

    @Test
    void shouldPopulateTTLOnOpenCase() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate expectedSystemTTL = now.toLocalDate().plusDays(180);
        Map<String, Object> expectedTTL = new HashMap<>();
        expectedTTL.put("OverrideTTL", null);
        expectedTTL.put("Suspend", "NO");
        expectedTTL.put("SystemTTL", expectedSystemTTL);

        caseDetails = CaseDetails.builder()
            .createdDate(now)
            .state("Open").build();

        assertThat(dataMigrationService.triggerTTLMigration(caseDetails).equals(expectedTTL));
    }

    @Test
    void shouldPopulateTTLOnSubmittedCase() {
        LocalDate now = LocalDate.now();
        LocalDate expectedSystemTTL = now.plusDays(6575);
        Map<String, Object> expectedTTL = new HashMap<>();
        expectedTTL.put("OverrideTTL", null);
        expectedTTL.put("Suspend", "NO");
        expectedTTL.put("SystemTTL", expectedSystemTTL);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("dateSubmitted", now);

        caseDetails = CaseDetails.builder()
            .data(caseData)
            .state("Submitted").build();

        assertThat(dataMigrationService.triggerTTLMigration(caseDetails).equals(expectedTTL));
    }

    @Test
    void shouldPopulateTTLOnClosedCase() {
        LocalDate now = LocalDate.now();
        LocalDate expectedSystemTTL = now.plusDays(6575);

        Map<String, Object> expectedTTL = new HashMap<>();
        expectedTTL.put("OverrideTTL", null);
        expectedTTL.put("Suspend", "NO");
        expectedTTL.put("SystemTTL", expectedSystemTTL);

        Map<String, Object> closeCase = new HashMap<>();
        closeCase.put("date", now.toString());

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("closeCase", closeCase);

        caseDetails = CaseDetails.builder()
            .data(caseData)
            .state("CLOSED").build();

        assertThat(dataMigrationService.triggerTTLMigration(caseDetails).equals(expectedTTL));
    }

    /*@Test
    void shouldPopulateTTLOnCaseManagementCase() {
        LocalDate now = LocalDate.now();
        LocalDate expectedSystemTTL = now.plusDays(6575);

        Map<String, Object> expectedTTL = new HashMap<>();
        expectedTTL.put("OverrideTTL", null);
        expectedTTL.put("Suspend", "NO");
        expectedTTL.put("SystemTTL", expectedSystemTTL);

        Map<String, Object> orderCollection = new HashMap<>();
        orderCollection.put("date", now.toString());

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("orderCollection", orderCollection);

        caseDetails = CaseDetails.builder()
            .data(caseData)
            .state("CASE_MANAGEMENT").build();

        assertThat(dataMigrationService.triggerTTLMigration(caseDetails).equals(expectedTTL));
    }*/

    @Test
    void shouldSetSuspendOnTTLCaseWithExistingTTL(){
        LocalDate now = LocalDate.now();
        LocalDate expectedSystemTTL = now.plusDays(6575);

        Map<String, Object> expectedTTL = new HashMap<>();
        expectedTTL.put("OverrideTTL", null);
        expectedTTL.put("Suspend", "YES");
        expectedTTL.put("SystemTTL", expectedSystemTTL);

        Map<String, Object> existingTTL = new HashMap<>();
        existingTTL.put("OverrideTTL", null);
        existingTTL.put("Suspend", "NO");
        existingTTL.put("SystemTTL", expectedSystemTTL);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("TTL", existingTTL);

        caseDetails = CaseDetails.builder()
            .data(caseData)
            .state("CASE_MANAGEMENT").build();

        assertThat(dataMigrationService.triggerSuspendMigrationTTL(caseDetails).equals(expectedTTL));
    }

    @Test
    void shouldSetSuspendOnTTLCaseWithoutExistingTTL(){
        Map<String, Object> expectedTTL = new HashMap<>();
        expectedTTL.put("OverrideTTL", null);
        expectedTTL.put("Suspend", "YES");
        expectedTTL.put("SystemTTL", null);

        Map<String, Object> caseData = new HashMap<>();

        caseDetails = CaseDetails.builder()
            .data(caseData)
            .state("CASE_MANAGEMENT").build();

        assertThat(dataMigrationService.triggerSuspendMigrationTTL(caseDetails).equals(expectedTTL));
    }
}
