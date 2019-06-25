package uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TelephoneNumber {
    private final String telephoneNumber;
    private final String telephoneUsageType;
    private final String contactDirection;
}
