package uk.gov.hmcts.reform.fpl.ccddatamigration.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OldChildren {
    private final OldChild firstChild;
    private final List<Map<String, Object>> additionalChildren;

    @JsonCreator
    public OldChildren(@JsonProperty("firstChild") OldChild firstChild,
                       @JsonProperty("additional") List<Map<String, Object>> additionalChildren) {
        this.firstChild  = firstChild;
        this.additionalChildren = additionalChildren;
    }
}
