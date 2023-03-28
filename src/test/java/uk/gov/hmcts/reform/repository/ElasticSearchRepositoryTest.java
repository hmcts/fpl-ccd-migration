package uk.gov.hmcts.reform.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.migration.repository.ElasticSearchRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticSearchRepositoryTest {

    private static final String USER_TOKEN = "TEST_USER_TOKEN";

    private static final String CASE_TYPE = "CASE_TYPE";

    private static final String AUTH_TOKEN = "Test_Auth_Token";

    private static final String INITIAL_QUERY = """
        {
         "query": {
           "bool": {
             "filter": [{
               "exists": {
                 "field": "data.hearingDetails"
               }
             }]
           }
         },
         "size": 100,
         "_source": [
           "reference",
           "jurisdiction"
         ],
         "sort": [
           {
             "reference.keyword": {
               "order": "asc"
             }
           }
         ]

        }""";

    private static final String SEARCH_AFTER_QUERY = """
        {
         "query": {
           "bool": {
             "filter": [{
               "exists": {
                 "field": "data.hearingDetails"
               }
             }]
           }
         },
         "size": 100,
         "_source": [
           "reference",
           "jurisdiction"
         ],
         "sort": [
           {
             "reference.keyword": {
               "order": "asc"
             }
           }
         ]
        ,"search_after": [1677777777]

        }""";

    private static final int QUERY_SIZE = 100;
    private static final int CASE_PROCESS_LIMIT = 100;

    private ElasticSearchRepository elasticSearchRepository;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        elasticSearchRepository = new ElasticSearchRepository(coreCaseDataApi,
                                                              authTokenGenerator,
                                                              QUERY_SIZE,
                                                              CASE_PROCESS_LIMIT);
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void shouldReturnSearchResultsForCaseTypeElasticSearch() {
        SearchResult searchResult = mock(SearchResult.class);
        when(coreCaseDataApi.searchCases(
            USER_TOKEN,
            AUTH_TOKEN,
            CASE_TYPE,
            INITIAL_QUERY
        )).thenReturn(searchResult);
        List<CaseDetails> caseDetails = elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE);
        assertThat(caseDetails).isNotNull();
        assertThat(caseDetails).isEmpty();
    }

    @Test
    void shouldNotReturnCaseDetailsForCaseTypeWhenSearchResultIsNull() {
        when(coreCaseDataApi.searchCases(
            USER_TOKEN,
            AUTH_TOKEN,
            CASE_TYPE,
            INITIAL_QUERY
        )).thenReturn(null);
        List<CaseDetails> caseDetails = elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE);
        assertThat(caseDetails).isNotNull();
        assertThat(caseDetails).isEmpty();
    }

    @Test
    void shouldReturnSearchResultsAndCaseDetailsForCaseTypeElasticSearch() {
        SearchResult searchResult = mock(SearchResult.class);
        List<CaseDetails> caseDetails = new ArrayList<>();
        CaseDetails details = mock(CaseDetails.class);
        when(details.getId()).thenReturn(1677777777L);
        caseDetails.add(details);
        when(searchResult.getCases()).thenReturn(caseDetails);
        when(searchResult.getTotal()).thenReturn(1);
        when(coreCaseDataApi.searchCases(
            USER_TOKEN,
            AUTH_TOKEN,
            CASE_TYPE,
            INITIAL_QUERY
        )).thenReturn(searchResult);

        SearchResult searchAfterResult = mock(SearchResult.class);
        when(coreCaseDataApi.searchCases(
            USER_TOKEN,
            AUTH_TOKEN,
            CASE_TYPE,
            SEARCH_AFTER_QUERY
        )).thenReturn(searchAfterResult);
        List<CaseDetails> caseDetails1 = new ArrayList<>();
        CaseDetails details1 = mock(CaseDetails.class);
        caseDetails1.add(details1);
        when(searchAfterResult.getCases()).thenReturn(caseDetails1);

        List<CaseDetails> returnCaseDetails = elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE);
        assertThat(returnCaseDetails).isNotNull();

        verify(authTokenGenerator, times(1)).generate();

        verify(coreCaseDataApi, times(1)).searchCases(USER_TOKEN,
                                                      AUTH_TOKEN,
                                                      CASE_TYPE,
                                                      INITIAL_QUERY);
        verify(coreCaseDataApi, times(1)).searchCases(USER_TOKEN,
                                                      AUTH_TOKEN,
                                                      CASE_TYPE,
                                                      SEARCH_AFTER_QUERY);
        assertThat(returnCaseDetails).hasSize(2);
    }

    @Test
    void shouldReturnOnlyInitialCaseDetailsWhenSearchAfterReturnsNullSearchResults() {
        SearchResult searchResult = mock(SearchResult.class);
        List<CaseDetails> caseDetails = new ArrayList<>();
        CaseDetails details = mock(CaseDetails.class);
        when(details.getId()).thenReturn(1677777777L);
        caseDetails.add(details);
        when(searchResult.getCases()).thenReturn(caseDetails);
        when(searchResult.getTotal()).thenReturn(1);
        when(coreCaseDataApi.searchCases(
            USER_TOKEN,
            AUTH_TOKEN,
            CASE_TYPE,
            INITIAL_QUERY
        )).thenReturn(searchResult);

        when(coreCaseDataApi.searchCases(
            USER_TOKEN,
            AUTH_TOKEN,
            CASE_TYPE,
            SEARCH_AFTER_QUERY
        )).thenReturn(null);

        List<CaseDetails> returnCaseDetails = elasticSearchRepository.findCaseByCaseType(USER_TOKEN, CASE_TYPE);
        assertThat(returnCaseDetails).isNotNull();

        verify(authTokenGenerator, times(1)).generate();

        verify(coreCaseDataApi, times(1)).searchCases(USER_TOKEN,
                                                      AUTH_TOKEN,
                                                      CASE_TYPE,
                                                      INITIAL_QUERY);
        verify(coreCaseDataApi, times(1)).searchCases(USER_TOKEN,
                                                      AUTH_TOKEN,
                                                      CASE_TYPE,
                                                      SEARCH_AFTER_QUERY);

        assertThat(returnCaseDetails).hasSize(1);
    }
}
