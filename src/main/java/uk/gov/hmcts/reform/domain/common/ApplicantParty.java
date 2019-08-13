package uk.gov.hmcts.reform.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApplicantParty {
    private final String partyId;
    private final String partyType;
    private final String organisationName;
    private final String pbaNumber;
    private final Address address;
    private final TelephoneNumber telephoneNumber;
    private final String jobTitle;
    private final TelephoneNumber mobileNumber;
    private final EmailAddress emailAddress;
}
