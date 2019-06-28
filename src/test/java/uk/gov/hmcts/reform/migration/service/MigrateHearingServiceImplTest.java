package uk.gov.hmcts.reform.migration.service;

import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import uk.gov.hmcts.reform.domain.Hearing;

import uk.gov.hmcts.reform.domain.OldHearing;

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

        service.migrate(caseDetails);

        Map<String, Object> valueInHearing = (Map<String, Object>) ((List) caseDetails.getData().get("hearings")).get(0);

        assertThat(valueInHearing.get("value").equals(newHearing));
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
            .reducedNoticeReason("reduced notice reason")
            .respondentsAware("respondents aware")
            .respondentsAwareReason("respondents aware reason")
            .build();
    }
}
