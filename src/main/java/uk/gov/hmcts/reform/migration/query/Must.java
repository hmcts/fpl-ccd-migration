package uk.gov.hmcts.reform.migration.query;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode
@Builder
public class Must implements EsClause {
    private final List<EsClause> clauses;

    @Override
    public Map<String, Object> toMap() {
        return Map.of("must", this.clauses.stream().map(EsClause::toMap).collect(Collectors.toList()));
    }
}
