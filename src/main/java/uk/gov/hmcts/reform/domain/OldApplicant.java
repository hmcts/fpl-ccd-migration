package uk.gov.hmcts.reform.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.Address;

@Data
@Builder
@AllArgsConstructor
public class OldApplicant {
    private final String name;
    private final String email;
    private final String mobile;
    private final Address address;
    private final String jobTitle;
    private final String telephone;
    private final String personToContact;
    private final String pbaNumber;
}
