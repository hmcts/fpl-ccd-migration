package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.migration.domain.OldRespondent;
import uk.gov.hmcts.reform.migration.domain.OldRespondents;
import uk.gov.hmcts.reform.migration.domain.common.Address;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RespondentsDataMigrationServiceTest {

    private RespondentsDataMigrationService service = new RespondentsDataMigrationService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMapCaseDetailsToNewCaseDetails() {
        Map<String, Object> data = new HashMap<>();

        OldRespondent respondent = OldRespondent.builder()
            .name("An Other")
            .dob("1999-12-31")
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

        data.put("respondents", OldRespondents.builder()
            .firstRespondent(respondent)
            .additional(ImmutableList.of(
                ImmutableMap.of(
                    "id", UUID.randomUUID(),
                    "value", respondent)))
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> respondents =
            objectMapper.convertValue(caseDetails.getData().get("respondents1"), List.class);

        assertThat(respondents).hasSize(2);
    }
}
