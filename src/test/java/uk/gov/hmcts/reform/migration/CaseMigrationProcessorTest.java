package uk.gov.hmcts.reform.migration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.PaginatedSearchMetadata;
import uk.gov.hmcts.reform.migration.ccd.CoreCaseDataService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CaseMigrationProcessorTest {

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
    private CaseMigrationProcessor caseMigrationProcessor;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CoreCaseDataApi ccdApi;

    private Map<String, String> searchCriteriaForPagination;
    private Map<String, String> searchCriteriaForCaseWorker;

    private CaseDetails caseDetails1;
    private CaseDetails caseDetails2;
    private CaseDetails caseDetails3;

    @Test
    public void shouldProcessASingleCaseAndMigrationIsSuccessful() {
        Map<String, Object> data = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();
        when(ccdApi.getCase(USER_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        caseMigrationProcessor.processSingleCase(USER_TOKEN, CASE_ID);
        verify(ccdApi, times(1)).getCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        assertNull(caseMigrationProcessor.getFailedCases());
        assertThat(caseMigrationProcessor.getMigratedCases(), is("1111"));
    }

    @Test
    public void shouldNotProcessASingleCaseWithOutRedundantFields() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .build();
        when(ccdApi.getCase(USER_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        caseMigrationProcessor.processSingleCase(USER_TOKEN, CASE_ID);
        verify(ccdApi, times(1)).getCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        assertNull(caseMigrationProcessor.getFailedCases());
        assertNull(caseMigrationProcessor.getMigratedCases());
    }

    @Test
    public void shouldProcessASingleCaseAndMigrationIsFailed() {
        Map<String, Object> data = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();
        when(coreCaseDataService.update(USER_TOKEN, caseDetails.getId().toString(),
                EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, caseDetails.getData()
        )).thenThrow(new RuntimeException("Internal server error"));
        when(ccdApi.getCase(USER_TOKEN, S2S_TOKEN, CASE_ID))
            .thenReturn(caseDetails);
        caseMigrationProcessor.processSingleCase(USER_TOKEN, CASE_ID);
        verify(ccdApi, times(1)).getCase(USER_TOKEN, S2S_TOKEN, CASE_ID);
        verify(coreCaseDataService, times(1)).update(USER_TOKEN, "1111", EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, caseDetails.getData()
        );
        assertThat(caseMigrationProcessor.getFailedCases(), is("1111"));
        assertNull(caseMigrationProcessor.getMigratedCases());
    }

    @Test
    public void shouldProcessAllTheCandidateCases_whenOneCaseFailed() {
        setupFields(true);
        setupMocks();
        setUpMockForUpdate(caseDetails1);
        setUpMockForUpdate(caseDetails2);
        when(coreCaseDataService.update(USER_TOKEN, caseDetails3.getId().toString(),
                EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, caseDetails3.getData()
        )).thenThrow(new RuntimeException("Internal server error"));
        caseMigrationProcessor.processAllCases(USER_TOKEN, USER_ID);
        assertThat(caseMigrationProcessor.getFailedCases(), is("1113"));
        assertThat(caseMigrationProcessor.getMigratedCases(), is("1111,1112"));
    }

    @Test
    public void shouldProcessAllTheCandidateCases_whenTwoCasesFailed() {
        setupFields(false);
        setupMocks();
        setUpMockForUpdate(caseDetails1);
        when(coreCaseDataService.update(USER_TOKEN, caseDetails2.getId().toString(),
                EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, caseDetails2.getData()
        )).thenThrow(new RuntimeException("Internal server error"));
        when(coreCaseDataService.update(USER_TOKEN, caseDetails3.getId().toString(),
                EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, caseDetails3.getData()
        )).thenThrow(new RuntimeException("Internal server error"));
        caseMigrationProcessor.processAllCases(USER_TOKEN, USER_ID);
        assertThat(caseMigrationProcessor.getFailedCases(), is("1112,1113"));
        assertThat(caseMigrationProcessor.getMigratedCases(), is("1111"));
    }

    @Test
    public void shouldProcessNoCaseWhenNoCasesAvailable() {
        setupFields(false);
        PaginatedSearchMetadata paginatedSearchMetadata = new PaginatedSearchMetadata();
        paginatedSearchMetadata.setTotalPagesCount(0);
        paginatedSearchMetadata.setTotalResultsCount(0);

        setupMocksForSearchCases(EMPTY_LIST, paginatedSearchMetadata);

        caseMigrationProcessor.processAllCases(USER_TOKEN, USER_ID);
        assertNull(caseMigrationProcessor.getFailedCases());
        assertNull(caseMigrationProcessor.getFailedCases());
    }

    private void setupMocks() {
        caseDetails1 = createCaseDetails(1111L, "FPL1");
        caseDetails2 = createCaseDetails(1112L, "FPL2");
        caseDetails3 = createCaseDetails(1113L, "FPL3");

        PaginatedSearchMetadata paginatedSearchMetadata = new PaginatedSearchMetadata();
        paginatedSearchMetadata.setTotalPagesCount(1);
        paginatedSearchMetadata.setTotalResultsCount(3);

        setupMocksForSearchCases(asList(caseDetails1, caseDetails2, caseDetails3), paginatedSearchMetadata);
    }

    private void setupFields(boolean debug) {
        if (debug) {
            Field debugEnabled = ReflectionUtils.findField(CaseMigrationProcessor.class, "debugEnabled");
            ReflectionUtils.makeAccessible(debugEnabled);
            ReflectionUtils.setField(debugEnabled, caseMigrationProcessor, debug);
        }
    }

    private void setUpMockForUpdate(CaseDetails caseDetails1) {
        when(coreCaseDataService.update(USER_TOKEN, caseDetails1.getId().toString(),
                EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, caseDetails1.getData()
        )).thenReturn(caseDetails1);
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

    private CaseDetails createCaseDetails(long id, String hwfQuestion) {
        Map<String, Object> data1 = new HashMap<>();
        data1.put("", hwfQuestion);
        return CaseDetails.builder()
            .id(id)
            .data(data1)
            .build();
    }
}
