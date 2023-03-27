package uk.gov.hmcts.reform.migration.query;

import lombok.Builder;

@Builder
public class ElasticSearchQuery {
    private static final String START_QUERY = """
        {
         "query": {
           "bool": {
            "filter":   {
                     "terms": {
                       "reference.keyword": [
                         "1651071080797567",
                         "1679579727556301"
                       ]
                     }
                   }

           }
         },
         "size": %s,
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
        """;

    private static final String END_QUERY = "\n}";

    private static final String SEARCH_AFTER = """
        "search_after": [%s]
        """;

    private String searchAfterValue;
    private int size;
    private boolean initialSearch;

    public String getQuery() {
        if (initialSearch) {
            return getInitialQuery();
        } else {
            return getSubsequentQuery();
        }
    }

    private String getInitialQuery() {
        return String.join("",
            String.format(START_QUERY, size),
            END_QUERY);
    }

    private String getSubsequentQuery() {
        return String.join("",
            String.format(START_QUERY, size),
            ",",
            String.format(SEARCH_AFTER, searchAfterValue),
            END_QUERY);
    }
}
