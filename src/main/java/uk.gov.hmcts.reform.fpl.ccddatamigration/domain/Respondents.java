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
public class Respondents {

    private final Respondent firstRespondent;
    private final List<Map<String, Object>> additional;

    @JsonCreator
    public Respondents(@JsonProperty("firstRespondent") Respondent firstRespondent,
                       @JsonProperty("additional") List<Map<String, Object>> additional) {
        this.firstRespondent = firstRespondent;
        this.additional = additional;
    }
}
