package uk.gov.hmcts.reform.fpl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.domain.common.RespondentParty;

@Data
@Builder
@AllArgsConstructor
public class Respondent {
    private final RespondentParty party;
    private final String leadRespondentIndicator;
}
