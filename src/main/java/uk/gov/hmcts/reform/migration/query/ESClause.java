package uk.gov.hmcts.reform.migration.query;

public interface ESClause<T> {
    T toMap();
}
