package uk.gov.hmcts.reform.fpl.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;

@Data
@Builder
@AllArgsConstructor
public class RespondentParty {
    private final String partyID;
    private final String partyType;
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final Address address;
    private final TelephoneNumber telephoneNumber;
    private final EmailAddress email;
    private final String gender;
    private final String genderIdentification;
    private final String placeOfBirth;
    private final String relationshipToChild;
    private final String contactDetailsHidden;
    private final String contactDetailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;
}
