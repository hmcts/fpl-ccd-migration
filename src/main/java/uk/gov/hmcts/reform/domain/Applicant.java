package uk.gov.hmcts.reform.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.Party;

@Data
@Builder
@AllArgsConstructor
public class Applicant {
    private final Party party;
    private final String leadApplicantIndicator;
}
