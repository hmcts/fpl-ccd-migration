package uk.gov.hmcts.reform.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.domain.exception.CaseMigrationException;
import uk.gov.hmcts.reform.migration.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.migration.repository.ElasticSearchRepository;
import uk.gov.hmcts.reform.migration.repository.IdamRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.migration.CaseMigrationProcessor.EVENT_DESCRIPTION;
import static uk.gov.hmcts.reform.migration.CaseMigrationProcessor.EVENT_ID;
import static uk.gov.hmcts.reform.migration.CaseMigrationProcessor.EVENT_SUMMARY;


@ExtendWith(MockitoExtension.class)
class CaseMigrationProcessorTest {

    private static final String USER_TOKEN = "Bearer eeeejjjttt";

    private static final String CASE_TYPE = "CARE_SUPERVISION_EPO";


    private CaseMigrationProcessor caseMigrationProcessor;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ElasticSearchRepository elasticSearchRepository;

    @Mock
    private IdamRepository idamRepository;

    @Captor
    ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    @BeforeEach
    void setUp() {
        caseMigrationProcessor = new CaseMigrationProcessor(coreCaseDataService,
            elasticSearchRepository,
            idamRepository,
            500,
            1,
            2,
            "DFPL-1124");
    }


    @Test
    void shouldMigrateCasesOfACaseTypeByParallelProcessing() throws InterruptedException {
        when(idamRepository.generateUserToken()).thenReturn(USER_TOKEN);
        List<CaseDetails> cases = createCaseDetails(1,2);
        when(elasticSearchRepository.fetchFirstPage(USER_TOKEN, CASE_TYPE, 2))
            .thenReturn(SearchResult.builder()
                .total(cases.size())
                .cases(cases)
                .build());
        caseMigrationProcessor.process(CASE_TYPE);
        verify(coreCaseDataService, times(2))
            .update(eq(USER_TOKEN),
                eq(EVENT_ID),
                eq(EVENT_SUMMARY),
                eq(EVENT_DESCRIPTION),
                eq(CASE_TYPE),
                caseDetailsArgumentCaptor.capture());

        assertThat(caseDetailsArgumentCaptor.getAllValues())
            .extracting("id")
            .contains(1L,2L);
    }

    @Test
    void shouldMigrateCasesOfACaseTypeByParallelProcessingElasticSearchWithNextPageCall()
        throws InterruptedException {
        when(idamRepository.generateUserToken()).thenReturn(USER_TOKEN);
        List<CaseDetails> cases = createCaseDetails(1, 2);
        when(elasticSearchRepository.fetchFirstPage(USER_TOKEN, CASE_TYPE, 2))
            .thenReturn(SearchResult.builder()
                .total(cases.size())
                .cases(cases)
                .build());
        when(elasticSearchRepository.fetchNextPage(USER_TOKEN, CASE_TYPE, "2", 2))
            .thenReturn(SearchResult.builder()
                .total(cases.size())
                .cases(createCaseDetails(3, 2))
                .build());

        when(elasticSearchRepository.fetchNextPage(USER_TOKEN, CASE_TYPE, "4", 2))
            .thenReturn(SearchResult.builder()
                .total(cases.size())
                .cases(createCaseDetails(5, 2))
                .build());

        when(elasticSearchRepository.fetchNextPage(USER_TOKEN, CASE_TYPE, "6", 2))
            .thenReturn(null);

        when(coreCaseDataService.update(eq(USER_TOKEN), eq(EVENT_ID), eq(EVENT_SUMMARY),
            eq(EVENT_DESCRIPTION), eq(CASE_TYPE), any()))
            .thenReturn(cases.get(0));

        caseMigrationProcessor.process(CASE_TYPE);

        verify(coreCaseDataService, times(6))
            .update(eq(USER_TOKEN),
                eq(EVENT_ID),
                eq(EVENT_SUMMARY),
                eq(EVENT_DESCRIPTION),
                eq(CASE_TYPE),
                caseDetailsArgumentCaptor.capture());

        assertThat(caseDetailsArgumentCaptor.getAllValues())
            .extracting("id")
            .contains(1L,2L,3L,4L,5L,6L);
    }

    private List<CaseDetails> createCaseDetails(int start, int count) {
        return LongStream.range(start, start + count)
            .mapToObj(counter -> CaseDetails.builder()
                .id(counter)
                .data(new HashMap<>())
                .build())
            .collect(toList());
    }

    @Test
    void shouldMigrateCasesOfACaseType() {
        when(idamRepository.generateUserToken()).thenReturn(USER_TOKEN);
        CaseDetails details = mock(CaseDetails.class);
        when(details.getId()).thenReturn(1677777777L);
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.add(details);
        when(elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE)).thenReturn(caseDetails);
        List<CaseDetails> listOfCaseDetails = elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE);
        assertNotNull(listOfCaseDetails);
        when(coreCaseDataService.update(USER_TOKEN, EVENT_ID, EVENT_SUMMARY,
            EVENT_DESCRIPTION, CASE_TYPE, details))
            .thenReturn(details);
        caseMigrationProcessor.migrateCases(CASE_TYPE);
        verify(coreCaseDataService, times(1))
            .update(USER_TOKEN,
                EVENT_ID,
                EVENT_SUMMARY,
                EVENT_DESCRIPTION,
                CASE_TYPE,
                details);
    }

    @Test
    void shouldMigrateOnlyLimitedNumberOfCases() {
        when(idamRepository.generateUserToken()).thenReturn(USER_TOKEN);
        CaseDetails details = mock(CaseDetails.class);
        when(details.getId()).thenReturn(1677777777L);
        CaseDetails details1 = mock(CaseDetails.class);
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.add(details);
        caseDetails.add(details1);
        when(elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE)).thenReturn(caseDetails);
        List<CaseDetails> listOfCaseDetails = elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE);
        assertNotNull(listOfCaseDetails);
        when(coreCaseDataService.update(USER_TOKEN, EVENT_ID, EVENT_SUMMARY,
            EVENT_DESCRIPTION, CASE_TYPE, details))
            .thenReturn(details);
        caseMigrationProcessor.migrateCases(CASE_TYPE);
        verify(coreCaseDataService, times(1))
            .update(USER_TOKEN,
                EVENT_ID,
                EVENT_SUMMARY,
                EVENT_DESCRIPTION,
                CASE_TYPE,
                details);
    }

    @Test
    void shouldThrowExceptionWhenMigrationIdNotSetForSingleThreadProcessing() {
        caseMigrationProcessor = new CaseMigrationProcessor(coreCaseDataService,
            elasticSearchRepository,
            idamRepository,
            500,
            1,
            10,
            null);
        assertThatThrownBy(() -> caseMigrationProcessor.migrateCases(null))
            .isInstanceOf(CaseMigrationException.class)
            .hasMessage("MigrationId is not set");
    }

    @Test
    void shouldThrowExceptionWhenMigrationIdNotSetForParallelProcessing() {
        caseMigrationProcessor = new CaseMigrationProcessor(coreCaseDataService,
            elasticSearchRepository,
            idamRepository,
            500,
            1,
            10,
            null);
        assertThatThrownBy(() -> caseMigrationProcessor.process(null))
            .isInstanceOf(CaseMigrationException.class)
            .hasMessage("MigrationId is not set");
    }

    @Test
    void shouldThrowExceptionWhenCaseTypeNullForSingleThreadProcessing() {
        assertThatThrownBy(() -> caseMigrationProcessor.migrateCases(null))
            .isInstanceOf(CaseMigrationException.class);
    }

    @Test
    void shouldThrowExceptionWhenCaseTypeNullForParallelProcessing() {
        assertThatThrownBy(() -> caseMigrationProcessor.process(null))
            .isInstanceOf(CaseMigrationException.class);
    }

    @Test
    void shouldThrowExceptionWhenMultipleCaseTypesPassedForSingleThreadProcessing() {
        assertThatThrownBy(() -> caseMigrationProcessor.migrateCases("Cast_Type1,Cast_Type2"))
            .isInstanceOf(CaseMigrationException.class);
    }

    @Test
    void shouldThrowExceptionWhenMultipleCaseTypesPassedForParallelProcessing() {
        assertThatThrownBy(() -> caseMigrationProcessor.process("Cast_Type1,Cast_Type2"))
            .isInstanceOf(CaseMigrationException.class);
    }
}
