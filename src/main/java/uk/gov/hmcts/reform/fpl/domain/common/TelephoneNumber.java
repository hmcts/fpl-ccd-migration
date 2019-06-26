package uk.gov.hmcts.reform.fpl.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TelephoneNumber {
    private final String telephoneNumber;
}
