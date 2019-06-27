package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldChild;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldChildren;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrateChildrenServiceTest {
    private final MigrateChildrenService service = new MigrateChildrenService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMapCaseDetailsToNewCaseDetails() {
        Map<String, Object> data = new HashMap<>();

        OldChild child = OldChild.builder()
                .childName("Child one")
                .childDOB("1999-12-31")
                .childGender("Boy")
                .childGenderIdentification("")
                .livingSituation("Living with parents")
                .situationDetails("")
                .situationDate("")
                .keyDates("")
                .careAndContact("")
                .adoption("")
                .placementOrderApplication("")
                .placementCourt("Craigavon")
                .mothersName("Mandy Burns")
                .fathersName("Ted Burns")
                .fathersResponsibility("Yes")
                .socialWorkerName("Laura Wilson")
                .socialWorkerTel("02838882333")
                .additionalNeeds("Yes")
                .additionalNeedsDetails("")
                .detailsHidden("Yes")
                .detailsHiddenReason("")
                .litigationIssues("Yes")
                .litigationIssuesDetails("")
                .address(Address.builder()
                        .addressLine1("1 some flat")
                        .addressLine2("1 some street")
                        .addressLine3("1 some road")
                        .county("some county")
                        .postTown("Craigavon")
                        .postcode("BT66 636")
                        .country("Northern Ireland")
                        .build()
                ).build();

        data.put("children", OldChildren.builder()
                .firstChild(child)
                .additionalChildren(ImmutableList.of(
                        ImmutableMap.of(
                                "id", UUID.randomUUID(),
                                "value", child)))

                .build());

        CaseDetails caseDetails = service.migrateCase(CaseDetails.builder()
                .id(1111L)
                .data(data)
                .build());

        List<Map<String, Object>> children = objectMapper.convertValue(caseDetails.getData().get("children1"),
                List.class);

        assertThat(children).hasSize(2);
    }
}


