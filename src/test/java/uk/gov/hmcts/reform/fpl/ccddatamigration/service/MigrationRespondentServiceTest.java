package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.Respondent;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.Respondents;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrationRespondentServiceTest {

    private final MigrateRespondentService service = new MigrateRespondentService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMapCaseDetailsToNewCaseDetails() {
        Map<String, Object> data = new HashMap<>();

        Respondent respondent = Respondent.builder()
            .name("An Other")
            .dob(new Date(System.currentTimeMillis()))
            .address(Address.builder()
                .addressLine1("address")
                .addressLine2("")
                .addressLine3("")
                .country("")
                .county("")
                .postcode("")
                .postTown("")
                .build())
            .contactDetailsHidden("")
            .gender("")
            .litigationIssues("")
            .placeOfBirth("")
            .relationshipToChild("")
            .telephone("")
            .build();

        data.put("respondents", Respondents.builder()
            .firstRespondent(respondent)
            .additional(ImmutableList.of(
                ImmutableMap.of(
                    "id", UUID.randomUUID(),
                    "value", respondent)))
            .build());

        CaseDetails caseDetails = service.migrateCase(CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build());

        List<Map<String, Object>> respondents =
            objectMapper.convertValue(caseDetails.getData().get("respondents1"), List.class);

        assertThat(respondents).hasSize(2);
    }
}
