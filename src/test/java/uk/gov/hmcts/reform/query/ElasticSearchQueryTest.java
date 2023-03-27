package uk.gov.hmcts.reform.query;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.migration.query.ElasticSearchQuery;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ElasticSearchQueryTest {

    private static final int QUERY_SIZE = 100;


    void shouldReturnQuery() {
        ElasticSearchQuery elasticSearchQuery =  ElasticSearchQuery.builder()
            .initialSearch(true)
            .size(QUERY_SIZE)
            .build();
        String query = elasticSearchQuery.getQuery();

        assertThat(query)
            .isEqualToIgnoringWhitespace("""
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
                 \s
                  }""");
    }


    void shouldReturnSearchAfterQuery() {
        ElasticSearchQuery elasticSearchQuery =  ElasticSearchQuery.builder()
            .initialSearch(false)
            .size(QUERY_SIZE)
            .searchAfterValue("1677777777")
            .build();
        String query = elasticSearchQuery.getQuery();

        assertThat(query)
            .isEqualToIgnoringWhitespace("""
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
                 \s
                  }
                """);
    }
}
