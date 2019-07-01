package uk.gov.hmcts.reform.migration.service;

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
    public void mapOldHearingToNewHearing() {
        Map<String, Object> data = new HashMap<>();

        OldHearing hearing = getOldHearing();

        Hearing newHearing = newHearingBuilder();

        data.put("hearing", hearing);

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

        String expectedDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        assertThat(actualNewHearing.getCreatedBy()).isEqualTo("TODO - CREATED BY");
        assertThat(actualNewHearing.getCreatedDate()).isEqualTo(expectedDate);

        assertThat(actualNewHearing.getUpdatedBy()).isNull();
        assertThat(actualNewHearing.getUpdatedDate()).isNull();
    }

    private Hearing newHearingBuilder() {
        Hearing newHearing = Hearing.builder().hearingDescription("hearing description").build();

        return newHearing;
    }

    private OldHearing getOldHearing() {

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
