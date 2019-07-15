package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.Hearing;
import uk.gov.hmcts.reform.domain.OldHearing;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class MigrateHearingServiceImpl implements DataMigrationService {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails != null && caseDetails.getData() != null &&
            !isEmpty(caseDetails.getData().get("hearing"));
    }

    @Override
    public void migrate(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        OldHearing oldHearing = objectMapper.convertValue(data.get("hearing"), OldHearing.class);

        Hearing migratedHearing = migrateHearing(oldHearing);

        data.put("hearing1", migratedHearing);
        data.put("hearing", null);

        log.info("New case details: {}", caseDetails);
    }

    private Hearing migrateHearing(OldHearing oldHearing) {
        log.info("Migrating hearing: {}", oldHearing);

        Hearing.HearingBuilder hearingBuilder = Hearing.builder();

        hearingBuilder.id(null);

        // description (type)
        hearingBuilder.description(defaultIfBlank(oldHearing.getType(), null));

        // reason (type_givereason)
        hearingBuilder.reason(defaultIfBlank(oldHearing.getType_GiveReason(), null));

        hearingBuilder.timeFrame(defaultIfBlank(oldHearing.getTimeFrame(), null));
        // same day hearing reason (reason)
        hearingBuilder.sameDayHearingReason(defaultIfBlank(oldHearing.getReason(), null));
        hearingBuilder.twoDayHearingReason(defaultIfBlank(oldHearing.getReason2Days(), null));
        hearingBuilder.sevenDayHearingReason(defaultIfBlank(oldHearing.getReason7Days(), null));
        hearingBuilder.twelveDayHearingReason(defaultIfBlank(oldHearing.getReason12Days(), null));

        hearingBuilder.withoutNotice(defaultIfBlank(oldHearing.getWithoutNotice(), null));
        // reason for no notice (without notice reason)
        hearingBuilder.reasonForNoNotice(defaultIfBlank(oldHearing.getWithoutNoticeReason(), null));
        hearingBuilder.reducedNotice(defaultIfBlank(oldHearing.getReducedNotice(), null));
        // reason for reduced notice (without reduced notice reason)
        hearingBuilder.reasonForReducedNotice(defaultIfBlank(oldHearing.getReducedNoticeReason(), null));
        hearingBuilder.respondentsAware(defaultIfBlank(oldHearing.getRespondentsAware(), null));
        // reason for respondants not being aware (respondents aware reason)
        hearingBuilder.reasonsForRespondentsNotBeingAware(defaultIfBlank(oldHearing.getRespondentsAwareReason(), null));

        return hearingBuilder.build();
    }

}
