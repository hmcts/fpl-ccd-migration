package uk.gov.hmcts.reform.fpl.ccddatamigration.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class NewRespondent {
    private final String partyType;
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final Address address;
}
