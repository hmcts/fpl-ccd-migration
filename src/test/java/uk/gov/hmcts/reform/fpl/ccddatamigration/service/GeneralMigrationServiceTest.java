package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.PaginatedSearchMetadata;
import uk.gov.hmcts.reform.fpl.ccddatamigration.ccd.CcdUpdateService;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldHearing;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GeneralMigrationServiceTest {

    private static final String USER_TOKEN = "Bearer eeeejjjttt";
    private static final String S2S_TOKEN = "eeeejjjttt";
    private static final String CASE_ID = "11111";
    private static final String USER_ID = "30";
    private static final String JURISDICTION_ID = "PUBLICLAW";
    private static final String CASE_TYPE = "CARE_SUPERVISION_EPO";
    private static final String EVENT_ID = "migrateCase";
    private static final String EVENT_SUMMARY = "Migrate Case";
    private static final String EVENT_DESCRIPTION = "Migrate Case";

    @InjectMocks
    private GeneralMigrationService migrationService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CoreCaseDataApi ccdApi;

    @Mock
    private MigrateHearingService migrateHearingService;

    private Map<String, String> searchCriteriaForPagination;
    private Map<String, String> searchCriteriaForCaseWorker;

    private CaseDetails caseDetails1;
    private CaseDetails caseDetails2;
    private CaseDetails caseDetails3;

    @Test
    public void shouldProcessASingleCaseAndMigrationIsSuccessful() {
        Map<String, Object> data = new HashMap<>();
        CaseDetails caseDetails = createCaseDetails(1111L);
        when(ccdApi.getCase(USER_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        when(migrateHearingService.migrateCase(any(CaseDetails.class))).thenReturn(caseDetails);
        migrationService.processSingleCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        verify(ccdApi, times(1)).getCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        assertThat(migrationService.getTotalNumberOfCases(), is(1));
        assertThat(migrationService.getTotalMigrationsPerformed(), is(1));
        assertNull(migrationService.getFailedCases());
        assertThat(migrationService.getMigratedCases(), is("1111"));
    }

    @Test
    public void shouldNotProcessASingleCaseWithOutRedundantFields() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .build();
        when(ccdApi.getCase(USER_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        migrationService.processSingleCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        verify(ccdApi, times(1)).getCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        assertThat(migrationService.getTotalNumberOfCases(), is(0));
        assertThat(migrationService.getTotalMigrationsPerformed(), is(0));
        assertNull(migrationService.getFailedCases());
        assertNull(migrationService.getMigratedCases());
    }

    @Test
    public void shouldProcessASingleCaseAndMigrationIsFailed() {
        Map<String, Object> data = new HashMap<>();
        CaseDetails caseDetails = createCaseDetails(1111L);
        when(ccdUpdateService.update(caseDetails.getId().toString(),
            caseDetails.getData(),
            EVENT_ID,
            USER_TOKEN,
            EVENT_SUMMARY,
            EVENT_DESCRIPTION)).thenThrow(new RuntimeException("Internal server error"));
        when(ccdApi.getCase(USER_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        when(migrateHearingService.migrateCase(any(CaseDetails.class))).thenReturn(caseDetails);
        migrationService.processSingleCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        verify(ccdApi, times(1)).getCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        verify(ccdUpdateService, times(1)).update("1111", caseDetails.getData(),
            EVENT_ID,
            USER_TOKEN,
            EVENT_SUMMARY,
            EVENT_DESCRIPTION);
        assertThat(migrationService.getTotalNumberOfCases(), is(1));
        assertThat(migrationService.getTotalMigrationsPerformed(), is(0));
        assertThat(migrationService.getFailedCases(), is("1111"));
        assertNull(migrationService.getMigratedCases());
    }



    @Test
    public void shouldProcessOnlyOneCandidateCase_whenDryRunIsTrue() {
        setupFields(true, true);
        setupMocks();
        when(migrateHearingService.migrateCase(eq(caseDetails1))).thenReturn(caseDetails1);
        migrationService.processAllTheCases(USER_TOKEN, S2S_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE);
        assertThat(migrationService.getTotalNumberOfCases(), is(1));
        assertThat(migrationService.getTotalMigrationsPerformed(), is(1));
        assertNull(migrationService.getFailedCases());
        assertThat(migrationService.getMigratedCases(), is("1111"));
    }



    @Test
    public void shouldProcessAllTheCandidateCases_whenDryRunIsFalseAndOneCaseFailed() {
        setupFields(false, true);
        setupMocks();
        setUpMockForUpdate(caseDetails1);
        setUpMockForUpdate(caseDetails2);
        when(migrateHearingService.migrateCase(eq(caseDetails1))).thenReturn(caseDetails1);
        when(migrateHearingService.migrateCase(eq(caseDetails2))).thenReturn(caseDetails2);
        when(migrateHearingService.migrateCase(eq(caseDetails3))).thenReturn(caseDetails3);
        when(ccdUpdateService.update(caseDetails3.getId().toString(),
            caseDetails3.getData(),
            EVENT_ID,
            USER_TOKEN,
            EVENT_SUMMARY,
            EVENT_DESCRIPTION)).thenThrow(new RuntimeException("Internal server error"));
        migrationService.processAllTheCases(USER_TOKEN, S2S_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE);
        assertThat(migrationService.getTotalNumberOfCases(), is(3));
        assertThat(migrationService.getTotalMigrationsPerformed(), is(2));
        assertThat(migrationService.getFailedCases(), is("1113"));
        assertThat(migrationService.getMigratedCases(), is("1111,1112"));
    }




    @Test
    public void shouldProcessAllTheCandidateCases_whenDryRunIsFalseAndTwoCasesFailed() {
        setupFields(false, false);
        setupMocks();
        setUpMockForUpdate(caseDetails1);
        when(migrateHearingService.migrateCase(eq(caseDetails1))).thenReturn(caseDetails1);
        when(migrateHearingService.migrateCase(eq(caseDetails2))).thenReturn(caseDetails2);
        when(migrateHearingService.migrateCase(eq(caseDetails3))).thenReturn(caseDetails3);
        when(ccdUpdateService.update(caseDetails2.getId().toString(),
            caseDetails2.getData(),
            EVENT_ID,
            USER_TOKEN,
            EVENT_SUMMARY,
            EVENT_DESCRIPTION)).thenThrow(new RuntimeException("Internal server error"));
        when(ccdUpdateService.update(caseDetails3.getId().toString(),
            caseDetails3.getData(),
            EVENT_ID,
            USER_TOKEN,
            EVENT_SUMMARY,
            EVENT_DESCRIPTION)).thenThrow(new RuntimeException("Internal server error"));
        migrationService.processAllTheCases(USER_TOKEN, S2S_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE);
        assertThat(migrationService.getTotalNumberOfCases(), is(3));
        assertThat(migrationService.getTotalMigrationsPerformed(), is(1));
        assertThat(migrationService.getFailedCases(), is("1112,1113"));
        assertThat(migrationService.getMigratedCases(), is("1111"));
    }

    @Test
    public void shouldProcessNoCaseWhenNoCasesAvailableWithDryRun() {
        setupFields(true, false);
        PaginatedSearchMetadata paginatedSearchMetadata = new PaginatedSearchMetadata();
        paginatedSearchMetadata.setTotalPagesCount(0);
        paginatedSearchMetadata.setTotalResultsCount(0);

        setupMocksForSearchCases(EMPTY_LIST, paginatedSearchMetadata);

        migrationService.processAllTheCases(USER_TOKEN, S2S_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE);
        assertThat(migrationService.getTotalNumberOfCases(), is(0));
        assertThat(migrationService.getTotalMigrationsPerformed(), is(0));
        assertNull(migrationService.getFailedCases());
        assertNull(migrationService.getFailedCases());
    }

    @Test
    public void shouldProcessNoCaseWhenNoCasesAvailableWithDryRunAsFalse() {
        setupFields(false, false);
        PaginatedSearchMetadata paginatedSearchMetadata = new PaginatedSearchMetadata();
        paginatedSearchMetadata.setTotalPagesCount(0);
        paginatedSearchMetadata.setTotalResultsCount(0);

        setupMocksForSearchCases(EMPTY_LIST, paginatedSearchMetadata);

        migrationService.processAllTheCases(USER_TOKEN, S2S_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE);
        assertThat(migrationService.getTotalNumberOfCases(), is(0));
        assertThat(migrationService.getTotalMigrationsPerformed(), is(0));
        assertNull(migrationService.getFailedCases());
        assertNull(migrationService.getFailedCases());
    }

    private void setupMocks() {
        caseDetails1 = createCaseDetails(1111L);
        caseDetails2 = createCaseDetails(1112L);
        caseDetails3 = createCaseDetails(1113L);

        PaginatedSearchMetadata paginatedSearchMetadata = new PaginatedSearchMetadata();
        paginatedSearchMetadata.setTotalPagesCount(1);
        paginatedSearchMetadata.setTotalResultsCount(3);

        setupMocksForSearchCases(asList(caseDetails1, caseDetails2, caseDetails3), paginatedSearchMetadata);
    }

    private void setupFields(boolean dryRun, boolean debug) {
        Field field = ReflectionUtils.findField(GeneralMigrationService.class, "dryRun");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, migrationService, dryRun);
        if (debug) {
            Field debugEnabled = ReflectionUtils.findField(GeneralMigrationService.class, "debugEnabled");
            ReflectionUtils.makeAccessible(debugEnabled);
            ReflectionUtils.setField(debugEnabled, migrationService, debug);
        }
    }

    private void setUpMockForUpdate(CaseDetails caseDetails1) {
        when(ccdUpdateService.update(caseDetails1.getId().toString(),
            caseDetails1.getData(),
            EVENT_ID,
            USER_TOKEN,
            EVENT_SUMMARY,
            EVENT_DESCRIPTION)).thenReturn(caseDetails1);
    }

    private void setupMocksForSearchCases(List<CaseDetails> caseDetails,
                                          PaginatedSearchMetadata paginatedSearchMetadata) {
        searchCriteriaForPagination = new HashMap<>();
        when(ccdApi.getPaginationInfoForSearchForCaseworkers(USER_TOKEN, S2S_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, searchCriteriaForPagination)).thenReturn(paginatedSearchMetadata);

        searchCriteriaForCaseWorker = new HashMap<>();
        searchCriteriaForCaseWorker.put("page", "1");

        when(ccdApi.searchForCaseworker(
            USER_TOKEN,
            S2S_TOKEN,
            USER_ID,
            JURISDICTION_ID,
            CASE_TYPE,
            searchCriteriaForCaseWorker))
            .thenReturn(caseDetails);
    }

    private CaseDetails createCaseDetails(long id) {

        Map<String, Object> data1 = new HashMap<>();

        OldHearing hearing = OldHearing.builder()
                .timeFrame("old timeframe")
                .reason("old reason")
                .type("old type")
                .type_GiveReason("old type give reason")
                .withoutNotice("old without notice")
                .withoutNoticeReason("old without notice reason")
                .reducedNotice("old reduced notice")
                .reducedNoticeReason("reduced notice reason")
                .respondentsAware("respondents aware")
                .respondentsAwareReason("respondents aware reason")
                .build();

        data1.put("hearing", ImmutableMap.of(
                "id", UUID.randomUUID().toString(),
                "value", hearing));

        return CaseDetails.builder()
                .id(id)
                .data(data1)
                .build();
    }
}
