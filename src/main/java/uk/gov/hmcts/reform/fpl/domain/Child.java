package uk.gov.hmcts.reform.fpl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.domain.common.ChildParty;

@Data
@Builder
@AllArgsConstructor
public class Child {
    private final ChildParty party;
}
