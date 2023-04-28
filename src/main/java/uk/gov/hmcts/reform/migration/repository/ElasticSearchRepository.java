package uk.gov.hmcts.reform.migration.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.migration.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.migration.query.ESQuery;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Repository
@Slf4j
public class ElasticSearchRepository {

    private final CoreCaseDataService ccdService;

    @Autowired
    public ElasticSearchRepository(CoreCaseDataService ccdService) {
        this.ccdService = ccdService;
    }

    public List<CaseDetails> search(String userToken, String caseType, ESQuery query, int size, int from) {
        requireNonNull(query);
        return search(userToken, caseType, query.toQueryContext(size, from).toString()).getCases();
    }

    public int searchResultsSize(String userToken, String caseType, ESQuery query) {
        requireNonNull(query);
        return search(userToken, caseType, query.toQueryContext(1, 0).toString()).getTotal();
    }

    private SearchResult search(String userToken, String caseType, String query) {
        return ccdService.searchCases(userToken, caseType, query);
    }
}
