package uk.gov.hmcts.reform.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.OldRespondent;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OldRespondents {

    private final OldRespondent firstRespondent;
    private final List<Map<String, Object>> additional;

    @JsonCreator
    public OldRespondents(@JsonProperty("firstRespondent") OldRespondent firstRespondent,
                          @JsonProperty("additional") List<Map<String, Object>> additional) {
        this.firstRespondent = firstRespondent;
        this.additional = additional;
    }
}
