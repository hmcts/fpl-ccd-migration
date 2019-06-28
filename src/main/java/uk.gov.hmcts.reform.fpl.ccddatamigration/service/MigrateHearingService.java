package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.Hearing;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldHearing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class MigrateHearingService {

    //private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ObjectMapper objectMapper;

    public CaseDetails migrateCase(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        // ADD NEW STRUCTURE TO CASE DATA

        Map<String, Object> hearingMapObject = objectMapper.convertValue(data.get("hearing"), Map.class);
        List<Map<String, Object>> listOfNewHearings = migrateHearing(hearingMapObject);
        data.put("hearingNew", listOfNewHearings);
        //data.put("hearingNew", migrateHearing(objectMapper.convertValue(data.get("hearing"), Map.class)));
        data.put("hearing", null);

        CaseDetails caseDetails1 = CaseDetails.builder()
                .data(data)
                .build();

        log.info("new case details: {}", caseDetails1);

        return caseDetails1;
    }

    private List<Map<String, Object>> migrateHearing(Map<String, Object> hearing) {
        log.info("beginning to migrate hearing {}", hearing);

        // HEARING
        OldHearing oldHearing = objectMapper.convertValue(hearing.get("hearing"), OldHearing.class);
        Hearing migratedHearing = migrateIndividualHearing(oldHearing);

        /// BUILD NEW STRUCTURE

        List<Map<String, Object>> newStructure = new ArrayList<>();

        newStructure.add(ImmutableMap.of(
                "id", UUID.randomUUID().toString(),
                "value", migratedHearing));

        log.info("returning new structure {}", newStructure);

        return newStructure;
    }

    private Hearing migrateIndividualHearing(OldHearing oldHearing) {
        log.info("migrating hearing {}", oldHearing);

        Hearing.HearingBuilder hearingBuilder = Hearing.builder();

        // description (type)
        hearingBuilder.hearingDescription(defaultIfBlank(oldHearing.getType(), null));

        // reason (type_givereason)
        hearingBuilder.reason(defaultIfBlank(oldHearing.getType_GiveReason(), null));

        hearingBuilder.timeFrame(defaultIfBlank(oldHearing.getTimeFrame(), null));
        // same day hearing reason (reason)
        hearingBuilder.sameDayHearingReason(defaultIfBlank(oldHearing.getReason(), null));
        hearingBuilder.withoutNotice(defaultIfBlank(oldHearing.getWithoutNotice(), null));
        // reason for no notice (without notice reason)
        hearingBuilder.reasonForNoNotice(defaultIfBlank(oldHearing.getWithoutNoticeReason(), null));
        hearingBuilder.reducedNotice(defaultIfBlank(oldHearing.getReducedNotice(), null));
        // reason for reduced notice (without reduced notice reason)
        hearingBuilder.reasonForReducedNotice(defaultIfBlank(oldHearing.getReducedNoticeReason(), null));
        hearingBuilder.respondentsAware(defaultIfBlank(oldHearing.getRespondentsAware(), null));
        // reason for respondants not being aware (respondents aware reason)
        hearingBuilder.reasonsForRespondentsNotBeingAware(defaultIfBlank(oldHearing.getRespondentsAwareReason(), null));

        // created by and when

        // updated by and when

        return hearingBuilder.build();
    }

}
