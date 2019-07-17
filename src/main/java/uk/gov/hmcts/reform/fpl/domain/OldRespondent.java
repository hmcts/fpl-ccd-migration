package uk.gov.hmcts.reform.fpl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.Address;

@Data
@Builder
@AllArgsConstructor
public class OldRespondent {
    private final String name;
    private final String dob;
    private final String gender;
    private final String genderIdentify;
    private final String placeOfBirth;
    private final String telephone;
    private final String relationshipToChild;
    private final String contactDetailsHidden;
    private final String contactDetailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;
    private final Address address;
}
