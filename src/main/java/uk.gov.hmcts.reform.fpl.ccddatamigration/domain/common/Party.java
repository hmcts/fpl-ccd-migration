package uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Party {
    private final String partyID;
    private final String partyType;
    private final String name;
    private final Address address;
    private final Email email;
    private final TelephoneNumber telephoneNumber;
    private final MobileNumber mobileNumber;
    private final JobTitle jobTitle;
}
