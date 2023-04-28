package uk.gov.hmcts.reform.migration.query;

import net.minidev.json.JSONObject;

import java.util.Map;

public interface ESQuery extends ESClause {
    default JSONObject toQueryContext(int size, int from) {
        return new JSONObject(Map.of("size", size, "from", from, "query", this.toMap()));
    }

    default JSONObject toQueryContext(int size, int from, Sort sort) {
        return new JSONObject(Map.of(
                "size", size,
                "from", from,
                "query", this.toMap(),
                "sort", sort.toMap())
        );
    }
}
