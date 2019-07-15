package uk.gov.hmcts.reform.migration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import uk.gov.hmcts.reform.domain.Hearing;

import uk.gov.hmcts.reform.domain.OldHearing;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class MigrateHearingServiceImplTest {

    private final MigrateHearingServiceImpl service = new MigrateHearingServiceImpl();

    @Test
    public void whenOldStructureDoesNotExistAcceptsShouldReturnFalse() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(ImmutableMap.of("data", "someData"))
            .build();

        assertThat(service.accepts().test(caseDetails)).isEqualTo(false);
    }

    @Test
    public void whenDataIsNullAcceptsShouldReturnFalse() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(null)
            .build();

        assertThat(service.accepts().test(caseDetails)).isEqualTo(false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenOldHearingStructureIsMigratedShouldReturnNewHearingDataStructure () {
        Map<String, Object> data = new HashMap<>();

        data.put("hearing", getOldHearing(false));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        caseDetails.setCreatedDate(LocalDateTime.now());

        service.migrate(caseDetails);

        Hearing actualNewHearing = (Hearing) caseDetails.getData().get("hearing1");

        // check fields are mapped to the new hearing correctly.
        assertThat(actualNewHearing.getDescription()).isEqualTo("old type");
        assertThat(actualNewHearing.getReason()).isEqualTo("old type give reason");
        assertThat(actualNewHearing.getTimeFrame()).isEqualTo("old timeframe");

        assertThat(actualNewHearing.getSameDayHearingReason()).isEqualTo("old same day reason");
        assertThat(actualNewHearing.getTwoDayHearingReason()).isEqualTo("old 2 day reason");
        assertThat(actualNewHearing.getSevenDayHearingReason()).isEqualTo("old 7 day reason");
        assertThat(actualNewHearing.getTwelveDayHearingReason()).isEqualTo("old 12 day reason");

        assertThat(actualNewHearing.getWithoutNotice()).isEqualTo("old without notice");
        assertThat(actualNewHearing.getReasonForNoNotice()).isEqualTo("old without notice reason");

        assertThat(actualNewHearing.getReducedNotice()).isEqualTo("old reduced notice");
        assertThat(actualNewHearing.getReasonForReducedNotice()).isEqualTo("old reduced notice reason");

        assertThat(actualNewHearing.getRespondentsAware()).isEqualTo("old respondents aware");
        assertThat(actualNewHearing.getReasonsForRespondentsNotBeingAware()).isEqualTo("old respondents aware reason");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenPartiallyFilledInOldHearingStructureIsMigratedShouldReturnNewHearingDataStructureWithNullFields () {
        Map<String, Object> data = new HashMap<>();

        OldHearing oldHearing = getOldHearing(true);

        data.put("hearing", oldHearing);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        caseDetails.setCreatedDate(LocalDateTime.now());

        service.migrate(caseDetails);

        Hearing actualNewHearing = (Hearing) caseDetails.getData().get("hearing1");

        // check fields are mapped to the new hearing correctly.
        assertThat(actualNewHearing.getDescription()).isNull();
        assertThat(actualNewHearing.getReason()).isNull();
        assertThat(actualNewHearing.getTimeFrame()).isNull();

        assertThat(actualNewHearing.getSameDayHearingReason()).isNull();

        assertThat(actualNewHearing.getWithoutNotice()).isNull();
        assertThat(actualNewHearing.getReasonForNoNotice()).isNull();

        assertThat(actualNewHearing.getReducedNotice()).isNull();
        assertThat(actualNewHearing.getReasonForReducedNotice()).isNull();

        assertThat(actualNewHearing.getRespondentsAware()).isNull();
        assertThat(actualNewHearing.getReasonsForRespondentsNotBeingAware()).isNull();
    }


    private OldHearing getOldHearing(boolean setFieldsToBeNull) {

        if(setFieldsToBeNull) {
            return OldHearing.builder().build();
        }

        return OldHearing.builder()
            .type("old type")
            .reason("old same day reason")
            .reason2Days("old 2 day reason")
            .reason7Days("old 7 day reason")
            .reason12Days("old 12 day reason")
            .timeFrame("old timeframe")
            .withoutNotice("old without notice")
            .withoutNoticeReason("old without notice reason")
            .reducedNotice("old reduced notice")
            .reducedNoticeReason("old reduced notice reason")
            .type_GiveReason("old type give reason")
            .respondentsAware("old respondents aware")
            .respondentsAwareReason("old respondents aware reason")
            .build();
    }
}
