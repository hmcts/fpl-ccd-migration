package uk.gov.hmcts.reform.migration.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.migration.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.migration.query.EsQuery;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Repository
@Slf4j
public class ElasticSearchRepository {

    private final CoreCaseDataService ccdService;

    @Autowired
    public ElasticSearchRepository(CoreCaseDataService ccdService) {
        this.ccdService = ccdService;
    }

    public int searchResultsSize(String userToken, String caseType, EsQuery query) {
        requireNonNull(query);
        return search(userToken, caseType, query.toQueryContext(1, 0).toString()).getTotal();
    }

    public SearchResult search(String userToken, String caseType, String query) {
        return ccdService.searchCases(userToken, caseType, query);
    }

    public List<CaseDetails> search(String userToken, String caseType, EsQuery query, int size, int from) {
        requireNonNull(query);
        SearchResult result = search(userToken, caseType, query.toQueryContext(size, from).toString());
        if (isEmpty(result)) {
            log.error("ES Query returned no cases, {}", query.toQueryContext(size, from));
            return List.of();
        }
        return result.getCases();
    }
}
