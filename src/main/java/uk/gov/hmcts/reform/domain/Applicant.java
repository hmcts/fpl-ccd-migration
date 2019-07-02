package uk.gov.hmcts.reform.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.ApplicantParty;

@Data
@Builder
@AllArgsConstructor
public class Applicant {
    private final ApplicantParty party;
    private final String leadApplicantIndicator;
}
