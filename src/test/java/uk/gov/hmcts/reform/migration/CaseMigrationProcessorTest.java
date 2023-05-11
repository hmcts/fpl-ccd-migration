package uk.gov.hmcts.reform.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.migration.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.migration.query.BooleanQuery;
import uk.gov.hmcts.reform.migration.query.EsQuery;
import uk.gov.hmcts.reform.migration.query.ExistsQuery;
import uk.gov.hmcts.reform.migration.query.Filter;
import uk.gov.hmcts.reform.migration.repository.ElasticSearchRepository;
import uk.gov.hmcts.reform.migration.repository.IdamRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private static final String CASE_JURISDICTION = "PUBLICLAW";

    private static final String MIGRATION_ID = "DFPL-TEST";

    private static final int DEFAUT_QUERY_SIZE = 10;
    private static final int DEFAULT_THREAD_LIMIT = 8;


    private CaseMigrationProcessor caseMigrationProcessor;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private ElasticSearchRepository elasticSearchRepository;

    @Mock
    private IdamRepository idamRepository;

    @Captor
    ArgumentCaptor<CaseDetails> caseDetailsArgumentCaptor;

    private static final EsQuery QUERY = BooleanQuery.builder()
        .filter(Filter.builder()
            .clauses(List.of(ExistsQuery.of("data.court")))
            .build())
        .build();

    @BeforeEach
    void setUp() {
        caseMigrationProcessor = new CaseMigrationProcessor(coreCaseDataService,
            elasticSearchRepository,
            idamRepository,
            DEFAUT_QUERY_SIZE,
            DEFAULT_THREAD_LIMIT,
            MIGRATION_ID);
    }

    @Test
    void shouldMigrateCasesOfACaseTypeByParallelProcessing() throws InterruptedException {
        when(idamRepository.generateUserToken()).thenReturn(USER_TOKEN);
        List<CaseDetails> cases = createCaseDetails(1,2);
        when(elasticSearchRepository.search(USER_TOKEN, CASE_TYPE, QUERY, DEFAUT_QUERY_SIZE, null))
            .thenReturn(cases);
        when(elasticSearchRepository.searchResultsSize(USER_TOKEN, CASE_TYPE, QUERY)).thenReturn(2);
        caseMigrationProcessor.migrateCases(CASE_TYPE, QUERY);
        verify(coreCaseDataService, times(2))
            .update(eq(USER_TOKEN),
                eq(EVENT_ID),
                eq(EVENT_SUMMARY),
                eq(EVENT_DESCRIPTION),
                eq(CASE_TYPE),
                caseDetailsArgumentCaptor.capture(),
                eq(MIGRATION_ID));

        assertThat(caseDetailsArgumentCaptor.getAllValues())
            .extracting("id")
            .contains(1L, 2L);
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
        when(elasticSearchRepository.search(USER_TOKEN, CASE_TYPE, QUERY, DEFAUT_QUERY_SIZE, null))
            .thenReturn(caseDetails);
        when(elasticSearchRepository.searchResultsSize(USER_TOKEN, CASE_TYPE, QUERY)).thenReturn(1);

        when(coreCaseDataService.update(USER_TOKEN, EVENT_ID, EVENT_SUMMARY,
            EVENT_DESCRIPTION, CASE_TYPE, details, MIGRATION_ID))
            .thenReturn(details);
        caseMigrationProcessor.migrateCases(CASE_TYPE, QUERY);
        verify(coreCaseDataService, times(1))
            .update(USER_TOKEN,
                EVENT_ID,
                EVENT_SUMMARY,
                EVENT_DESCRIPTION,
                CASE_TYPE,
                details,
                MIGRATION_ID);
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
        when(elasticSearchRepository.search(USER_TOKEN, CASE_TYPE, QUERY, DEFAUT_QUERY_SIZE, null))
            .thenReturn(caseDetails);
        when(elasticSearchRepository.searchResultsSize(USER_TOKEN, CASE_TYPE, QUERY)).thenReturn(1);

        when(coreCaseDataService.update(USER_TOKEN, EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, CASE_TYPE, details,
            MIGRATION_ID))
            .thenReturn(details);
        caseMigrationProcessor.migrateCases(CASE_TYPE, QUERY);
        verify(coreCaseDataService, times(1))
            .update(USER_TOKEN,
                EVENT_ID,
                EVENT_SUMMARY,
                EVENT_DESCRIPTION,
                CASE_TYPE,
                details,
                MIGRATION_ID);
    }

    @Test
    void shouldThrowExceptionWhenCaseTypeIsNull() {
        caseMigrationProcessor = new CaseMigrationProcessor(coreCaseDataService,
            elasticSearchRepository,
            idamRepository,
            10,
            DEFAULT_THREAD_LIMIT,
            "Test");
        assertThatThrownBy(() -> caseMigrationProcessor.migrateCases(null, BooleanQuery.builder().build()))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenQueryIsNull() {
        caseMigrationProcessor = new CaseMigrationProcessor(coreCaseDataService,
            elasticSearchRepository,
            idamRepository,
            10,
            DEFAULT_THREAD_LIMIT,
            "Test");
        assertThatThrownBy(() -> caseMigrationProcessor.migrateCases("TEST", null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenMigrationIdIsNull() {
        caseMigrationProcessor = new CaseMigrationProcessor(coreCaseDataService,
            elasticSearchRepository,
            idamRepository,
            10,
            DEFAULT_THREAD_LIMIT,
            null);
        assertThatThrownBy(() -> caseMigrationProcessor.migrateCases("Test", BooleanQuery.builder().build()))
            .isInstanceOf(NullPointerException.class);
    }

    @Nested
    class MigrateCaseList {

        @Test
        void shouldMigrateOnlySelectCases() {
            when(idamRepository.generateUserToken()).thenReturn(USER_TOKEN);

            List<String> caseIds = List.of("12345", "67890");

            caseMigrationProcessor.migrateList(CASE_TYPE, CASE_JURISDICTION, caseIds);

            verify(coreCaseDataService, times(2))
                .update(eq(USER_TOKEN),
                    eq(EVENT_ID),
                    eq(EVENT_SUMMARY),
                    eq(EVENT_DESCRIPTION),
                    eq(CASE_TYPE),
                    any(),
                    eq(MIGRATION_ID));
        }

    }
}
