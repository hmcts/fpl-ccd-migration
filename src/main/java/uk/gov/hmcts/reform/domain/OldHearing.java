package uk.gov.hmcts.reform.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OldHearing {

    private final String type;
    private final String reason; // same day
    private final String reason2Days;
    private final String reason7Days;
    private final String reason12Days;
    private final String timeFrame;
    private final String reducedNotice;
    private final String reducedNoticeReason;
    private final String withoutNotice;
    private final String withoutNoticeReason;
    private final String type_GiveReason;
    private final String respondentsAware;
    private final String respondentsAwareReason;
}
