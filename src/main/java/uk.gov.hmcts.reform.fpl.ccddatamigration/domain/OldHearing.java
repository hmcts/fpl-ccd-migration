package uk.gov.hmcts.reform.fpl.ccddatamigration.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OldHearing {

    private final String timeFrame;
    private final String reason;
    private final String type;
    private final String type_GiveReason;
    private final String withoutNotice;
    private final String withoutNoticeReason;
    private final String reducedNotice;
    private final String reducedNoticeReason;
    private final String respondentsAware;
    private final String respondentsAwareReason;

    @JsonCreator
    public OldHearing(@JsonProperty("timeFrame") String timeFrame,
                      @JsonProperty("reason") String reason,
                      @JsonProperty("type") String type,
                      @JsonProperty("type_GiveReason") String type_GiveReason,
                      @JsonProperty("withoutNotice") String withoutNotice,
                      @JsonProperty("withoutNoticeReason") String withoutNoticeReason,
                      @JsonProperty("reducedNotice") String reducedNotice,
                      @JsonProperty("reducedNoticeReason") String reducedNoticeReason,
                      @JsonProperty("respondentsAware") String respondentsAware,
                      @JsonProperty("respondentsAwareReason") String respondentsAwareReason) {
        this.timeFrame = timeFrame;
        this.reason = reason;
        this.type = type;
        this.type_GiveReason = type_GiveReason;
        this.withoutNotice = withoutNotice;
        this.withoutNoticeReason = withoutNoticeReason;
        this.reducedNotice = reducedNotice;
        this.reducedNoticeReason = reducedNoticeReason;
        this.respondentsAware = respondentsAware;
        this.respondentsAwareReason = respondentsAwareReason;
    }
}
