package uk.gov.hmcts.reform.migration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import uk.gov.hmcts.reform.domain.Hearing;

import uk.gov.hmcts.reform.domain.OldHearing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class MigrateHearingServiceImplTest {

    @InjectMocks
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

    @Test
    public void whenOldHearingStructureIsMigratedShouldReturnNewHearingDataStructure () {
        Map<String, Object> data = new HashMap<>();

        OldHearing oldHearing = getOldHearing(false);

        Hearing newHearing = newHearingBuilder();

        data.put("hearing", oldHearing);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        caseDetails.setCreatedDate(LocalDateTime.now());

        service.migrate(caseDetails);

        Map<String, Object> valueInHearing = (Map<String, Object>) ((List) caseDetails.getData().get("hearings")).get(0);

        Hearing actualNewHearing = (Hearing) valueInHearing.get("value");
        assertThat(actualNewHearing.equals(newHearing));

        // check fields are mapped to the new hearing correctly.
        assertThat(actualNewHearing.getHearingDescription()).isEqualTo("old type");
        assertThat(actualNewHearing.getReason()).isEqualTo("old type give reason");
        assertThat(actualNewHearing.getTimeFrame()).isEqualTo("old timeframe");

        assertThat(actualNewHearing.getSameDayHearingReason()).isEqualTo("old reason");

        assertThat(actualNewHearing.getWithoutNotice()).isEqualTo("old without notice");
        assertThat(actualNewHearing.getReasonForNoNotice()).isEqualTo("old without notice reason");

        assertThat(actualNewHearing.getReducedNotice()).isEqualTo("old reduced notice");
        assertThat(actualNewHearing.getReasonForReducedNotice()).isEqualTo("old reduced notice reason");

        assertThat(actualNewHearing.getRespondentsAware()).isEqualTo("old respondents aware");
        assertThat(actualNewHearing.getReasonsForRespondentsNotBeingAware()).isEqualTo("old respondents aware reason");

        assertThat(actualNewHearing.getCreatedBy()).isNull();
        assertThat(actualNewHearing.getCreatedDate()).isNull();

        assertThat(actualNewHearing.getUpdatedBy()).isNull();
        assertThat(actualNewHearing.getUpdatedDate()).isNull();
    }


    @Test
    public void whenPartiallyFilledInOldHearingStructureIsMigratedShouldReturnNewHearingDataStructureWithNullFields () {
        Map<String, Object> data = new HashMap<>();

        OldHearing oldHearing = getOldHearing(true);

        Hearing newHearing = newHearingBuilder();

        data.put("hearing", oldHearing);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        caseDetails.setCreatedDate(LocalDateTime.now());

        service.migrate(caseDetails);

        Map<String, Object> valueInHearing = (Map<String, Object>) ((List) caseDetails.getData().get("hearings")).get(0);

        Hearing actualNewHearing = (Hearing) valueInHearing.get("value");
        assertThat(actualNewHearing.equals(newHearing));

        // check fields are mapped to the new hearing correctly.
        assertThat(actualNewHearing.getHearingDescription()).isNull();
        assertThat(actualNewHearing.getReason()).isNull();
        assertThat(actualNewHearing.getTimeFrame()).isNull();

        assertThat(actualNewHearing.getSameDayHearingReason()).isNull();

        assertThat(actualNewHearing.getWithoutNotice()).isNull();
        assertThat(actualNewHearing.getReasonForNoNotice()).isNull();

        assertThat(actualNewHearing.getReducedNotice()).isNull();
        assertThat(actualNewHearing.getReasonForReducedNotice()).isNull();

        assertThat(actualNewHearing.getRespondentsAware()).isNull();
        assertThat(actualNewHearing.getReasonsForRespondentsNotBeingAware()).isNull();

        assertThat(actualNewHearing.getCreatedBy()).isNull();
        assertThat(actualNewHearing.getCreatedDate()).isNull();

        assertThat(actualNewHearing.getUpdatedBy()).isNull();
        assertThat(actualNewHearing.getUpdatedDate()).isNull();

    }

    private Hearing newHearingBuilder() {
        Hearing newHearing = Hearing.builder().hearingDescription("hearing description").build();
        return newHearing;
    }

    private OldHearing getOldHearing(boolean setFieldsToBeNull) {

        if(setFieldsToBeNull) {
            return OldHearing.builder().build();
        }

        return OldHearing.builder()
            .timeFrame("old timeframe")
            .reason("old reason")
            .type("old type")
            .type_GiveReason("old type give reason")
            .withoutNotice("old without notice")
            .withoutNoticeReason("old without notice reason")
            .reducedNotice("old reduced notice")
            .reducedNoticeReason("old reduced notice reason")
            .respondentsAware("old respondents aware")
            .respondentsAwareReason("old respondents aware reason")
            .build();
    }
}
