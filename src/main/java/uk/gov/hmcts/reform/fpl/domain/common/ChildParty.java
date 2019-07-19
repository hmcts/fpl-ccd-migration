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
public class ChildParty {
    private final String partyID;
    private final PartyType partyType;
    private final String firstName;
    private final String lastName;
    private final String organisationName;
    private final String dateOfBirth;
    private final Address address;
    private final EmailAddress email;
    private final String gender;
    private final String genderIdentification;
    private final String livingSituation;
    private final String situationDetails;
    private final String situationDate;
    private final String keyDates;
    private final String careAndContact;
    private final String adoption;
    private final String placementOrderApplication;
    private final String placementCourt;
    private final String mothersName;
    private final String fathersName;
    private final String fathersResponsibility;
    private final String socialWorkerName;
    private final TelephoneNumber socialWorkerTel;
    private final String additionalNeeds;
    private final String additionalNeedsDetails;
    private final String detailsHidden;
    private final String detailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;
    private final TelephoneNumber telephoneNumber;
}
