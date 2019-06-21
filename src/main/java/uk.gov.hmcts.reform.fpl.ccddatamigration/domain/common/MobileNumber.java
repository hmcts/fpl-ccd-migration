package uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MobileNumber {
    private final String telephoneNumber;
}
