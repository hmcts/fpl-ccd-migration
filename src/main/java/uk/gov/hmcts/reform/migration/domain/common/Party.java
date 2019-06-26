package uk.gov.hmcts.reform.migration.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Party {
    private final String partyID;
    private final String partyType;
    private final String title;
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
    private final String litigationIssues;
    private final String litigationIssuesDetails;
}
