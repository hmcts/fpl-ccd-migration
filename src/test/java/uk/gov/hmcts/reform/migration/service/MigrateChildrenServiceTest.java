package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;
import uk.gov.hmcts.reform.fpl.domain.Child;
import uk.gov.hmcts.reform.fpl.domain.OldChild;
import uk.gov.hmcts.reform.fpl.domain.OldChildren;
import uk.gov.hmcts.reform.fpl.domain.common.ChildParty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MigrateChildrenServiceTest {
    private final MigrateChildrenService service = new MigrateChildrenService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void whenOldStructureDoesNotExistAcceptsShouldReturnFalse() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(ImmutableMap.of("data", "someData"))
            .build();

        assertThat(service.accepts().test(caseDetails)).isEqualTo(false);
    }

    @Test
    void whenDataIsNullAcceptsShouldReturnFalse() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(null)
            .build();

        assertThat(service.accepts().test(caseDetails)).isEqualTo(false);
    }

    @SuppressWarnings({"unchecked", "LineLength"})
    @Test
    void whenOldChildrenStructureIsMigratedShouldReturnNewListStructure() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(createOldChild(false, false))
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> children = (List<Map<String, Object>>) caseDetails.getData().get("children1");

        Map<String, Object> firstChild = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> firstChildParty = (Map<String, Object>) firstChild.get("party");
        Child expectedChild = createNewChild();

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.get("partyID")).isInstanceOf(String.class);
        assertThat(firstChildParty.get("partyType")).isNull();
        assertThat(firstChildParty.get("firstName")).isEqualTo(expectedChild.getParty().getFirstName());
        assertThat(firstChildParty.get("lastName")).isEqualTo(expectedChild.getParty().getLastName());
        assertThat(firstChildParty.get("dateOfBirth")).isEqualTo(expectedChild.getParty().getDateOfBirth());
        assertThat(objectMapper.convertValue(firstChildParty.get("address"), Address.class)).isEqualTo(expectedChild.getParty().getAddress());
        assertThat(firstChildParty.get("gender")).isEqualTo(expectedChild.getParty().getGender());
        assertThat(firstChildParty.get("genderIdentification")).isNull();
        assertThat(firstChildParty.get("litigationIssues")).isEqualTo(expectedChild.getParty().getLitigationIssues());
        assertThat(firstChildParty.get("litigationIssuesDetails")).isEqualTo(expectedChild.getParty().getLitigationIssuesDetails());
        assertThat(firstChildParty.get("livingSituation")).isEqualTo(expectedChild.getParty().getLivingSituation());
        assertThat(firstChildParty.get("situationDetails")).isEqualTo(expectedChild.getParty().getSituationDetails());
        assertThat(firstChildParty.get("situationDate")).isEqualTo(expectedChild.getParty().getSituationDate());
        assertThat(firstChildParty.get("keyDates")).isNull();
        assertThat(firstChildParty.get("careAndContact")).isNull();
        assertThat(firstChildParty.get("adoption")).isNull();
        assertThat(firstChildParty.get("placementOrderApplication")).isNull();
        assertThat(firstChildParty.get("placementCourt")).isEqualTo(expectedChild.getParty().getPlacementCourt());
        assertThat(firstChildParty.get("mothersName")).isEqualTo(expectedChild.getParty().getMothersName());
        assertThat(firstChildParty.get("fathersName")).isEqualTo(expectedChild.getParty().getFathersName());
        assertThat(firstChildParty.get("fathersResponsibility")).isEqualTo(expectedChild.getParty().getFathersResponsibility());
        assertThat(firstChildParty.get("socialWorkerName")).isEqualTo(expectedChild.getParty().getSocialWorkerName());
        assertThat(firstChildParty.get("socialWorkerTel")).isEqualTo(objectMapper.convertValue(expectedChild.getParty().getSocialWorkerTel(), Object.class));
        assertThat(firstChildParty.get("additionalNeeds")).isEqualTo(expectedChild.getParty().getAdditionalNeeds());
        assertThat(firstChildParty.get("additionalNeedsDetails")).isEqualTo(expectedChild.getParty().getAdditionalNeedsDetails());
        assertThat(firstChildParty.get("detailsHidden")).isEqualTo(expectedChild.getParty().getDetailsHidden());
        assertThat(firstChildParty.get("detailsHiddenReason")).isEqualTo(expectedChild.getParty().getDetailsHiddenReason());
    }

    @Test
    void whenPartiallyFilledInOldChildStructureIsMigratedShouldReturnNewListStructureWithNullFields() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(createOldChild(false, true))
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> children = (List<Map<String, Object>>) caseDetails.getData().get("children1");

        Map<String, Object> firstChild = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> firstChildParty = (Map<String, Object>) firstChild.get("party");
        Child expectedChild = createNewChild();

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.get("partyID")).isInstanceOf(String.class);
        assertThat(firstChildParty.get("partyType")).isNull();
        assertThat(firstChildParty.get("firstName")).isEqualTo(expectedChild.getParty().getFirstName());
        assertThat(firstChildParty.get("lastName")).isEqualTo(expectedChild.getParty().getLastName());
        assertThat(firstChildParty.get("dateOfBirth")).isNull();
        assertThat(objectMapper.convertValue(firstChildParty.get("address"), Address.class)).isEqualTo(Address.builder().build());
        assertThat(firstChildParty.get("gender")).isNull();
        assertThat(firstChildParty.get("genderIdentification")).isNull();
        assertThat(firstChildParty.get("litigationIssues")).isNull();
        assertThat(firstChildParty.get("litigationIssuesDetails")).isNull();
        assertThat(firstChildParty.get("livingSituation")).isNull();
        assertThat(firstChildParty.get("situationDetails")).isNull();
        assertThat(firstChildParty.get("situationDate")).isNull();
        assertThat(firstChildParty.get("keyDates")).isNull();
        assertThat(firstChildParty.get("careAndContact")).isNull();
        assertThat(firstChildParty.get("adoption")).isNull();
        assertThat(firstChildParty.get("placementOrderApplication")).isNull();
        assertThat(firstChildParty.get("placementCourt")).isNull();
        assertThat(firstChildParty.get("mothersName")).isNull();
        assertThat(firstChildParty.get("fathersName")).isNull();
        assertThat(firstChildParty.get("fathersResponsibility")).isNull();
        assertThat(firstChildParty.get("socialWorkerName")).isNull();
        assertThat(firstChildParty.get("socialWorkerTel")).isNull();
        assertThat(firstChildParty.get("additionalNeeds")).isNull();
        assertThat(firstChildParty.get("additionalNeedsDetails")).isNull();
        assertThat(firstChildParty.get("detailsHidden")).isNull();
        assertThat(firstChildParty.get("detailsHiddenReason")).isNull();
    }

    @Test
    void whenPartiallyFilledInOldChildAddressIsMigratedShouldReturnNewListStructureWithNullAddressFields() {
        Map<String, Object> data = new HashMap<>();

        OldChild child = OldChild.builder()
            .address(Address.builder()
                .postcode("postcode")
                .county("Kent")
                .build())
            .build();

        data.put("children", OldChildren.builder()
            .firstChild(child)
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> children = (List<Map<String, Object>>) caseDetails.getData().get("children1");

        Map<String, Object> firstChild = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> firstChildParty = (Map<String, Object>) firstChild.get("party");

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.get("partyType")).isNull();
        assertThat(firstChildParty.get("firstName")).isNull();
        assertThat(firstChildParty.get("lastName")).isNull();
        assertThat(firstChildParty.get("dateOfBirth")).isNull();
        assertThat(objectMapper.convertValue(firstChildParty.get("address"), Address.class)).isEqualTo(Address.builder()
            .addressLine1(null)
            .addressLine2(null)
            .addressLine3(null)
            .postcode("postcode")
            .county("Kent")
            .country(null)
            .postTown(null)
            .build());
        assertThat(firstChildParty.get("gender")).isNull();
        assertThat(firstChildParty.get("genderIdentification")).isNull();
        assertThat(firstChildParty.get("litigationIssues")).isNull();
        assertThat(firstChildParty.get("litigationIssuesDetails")).isNull();
        assertThat(firstChildParty.get("livingSituation")).isNull();
        assertThat(firstChildParty.get("situationDetails")).isNull();
        assertThat(firstChildParty.get("situationDate")).isNull();
        assertThat(firstChildParty.get("keyDates")).isNull();
        assertThat(firstChildParty.get("careAndContact")).isNull();
        assertThat(firstChildParty.get("adoption")).isNull();
        assertThat(firstChildParty.get("placementOrderApplication")).isNull();
        assertThat(firstChildParty.get("placementCourt")).isNull();
        assertThat(firstChildParty.get("mothersName")).isNull();
        assertThat(firstChildParty.get("fathersName")).isNull();
        assertThat(firstChildParty.get("fathersResponsibility")).isNull();
        assertThat(firstChildParty.get("socialWorkerName")).isNull();
        assertThat(firstChildParty.get("socialWorkerTel")).isNull();
        assertThat(firstChildParty.get("additionalNeeds")).isNull();
        assertThat(firstChildParty.get("additionalNeedsDetails")).isNull();
        assertThat(firstChildParty.get("detailsHidden")).isNull();
        assertThat(firstChildParty.get("detailsHiddenReason")).isNull();
    }

    @Test
    void whenOldChildWithOneNameIsMigratedShouldReturnNewListStructureWithOnlyFirstName() {
        Map<String, Object> data = new HashMap<>();

        OldChild child = OldChild.builder()
            .childName("Noel")
            .build();

        data.put("children", OldChildren.builder()
            .firstChild(child)
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> children = (List<Map<String, Object>>) caseDetails.getData().get("children1");

        Map<String, Object> firstChild = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> firstChildParty = (Map<String, Object>) firstChild.get("party");

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.get("firstName")).isEqualTo("Noel");
        assertThat(firstChildParty.get("lastName")).isNull();
    }

    @Test
    void whenOldChildWithManyNamesIsMigratedShouldReturnNewListStructureWithMultipleFirstNames() {
        Map<String, Object> data = new HashMap<>();

        OldChild child = OldChild.builder()
            .childName("Jean Paul Gautier")
            .build();

        data.put("children", OldChildren.builder()
            .firstChild(child)
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> children = (List<Map<String, Object>>) caseDetails.getData().get("children1");

        Map<String, Object> firstChild = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> firstChildParty = (Map<String, Object>) firstChild.get("party");

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.get("firstName")).isEqualTo("Jean Paul");
        assertThat(firstChildParty.get("lastName")).isEqualTo("Gautier");
    }

    private Map<String, Object> createOldChild(boolean multiple, boolean partial) {
        Map<String, Object> data = new HashMap<>();
        OldChild child;

        if (partial) {
            child = OldChild.builder()
                .childName("Child One")
                .build();
        } else {
            child = OldChild.builder()
                .childName("Child One")
                .childDOB("1999-12-31")
                .childGender("Male")
                .childGenderIdentification("")
                .livingSituation("Living with parents")
                .situationDetails("situation details")
                .situationDate("situation date")
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
                .additionalNeedsDetails("additional needs details")
                .detailsHidden("Yes")
                .detailsHiddenReason("details hidden reason")
                .litigationIssues("Yes")
                .litigationIssuesDetails("litigation issue details")
                .address(Address.builder()
                    .addressLine1("Building number")
                    .addressLine2("Street Name")
                    .addressLine3("Other street Name")
                    .country("Country")
                    .county("County")
                    .postcode("AA1 1AA")
                    .postTown("Town")
                    .build()
                ).build();
        }

        if (multiple) {
            data.put("children", OldChildren.builder()
                .firstChild(child)
                .additionalChildren(ImmutableList.of(
                    ImmutableMap.of(
                        "id", "",
                        "value", child)))
                .build());
        } else {
            data.put("children", OldChildren.builder()
                .firstChild(child)
                .build());
        }

        return data;
    }

    private Child createNewChild() {
        return Child.builder()
            .party(ChildParty.builder()
                .partyID("")
                .firstName("Child")
                .lastName("One")
                .address(Address.builder()
                    .addressLine1("Building number")
                    .addressLine2("Street Name")
                    .addressLine3("Other street Name")
                    .country("Country")
                    .county("County")
                    .postcode("AA1 1AA")
                    .postTown("Town")
                    .build())
                .gender("Male")
                .genderIdentification("")
                .litigationIssues("Yes")
                .litigationIssuesDetails("litigation issue details")
                .dateOfBirth("1999-12-31")
                .livingSituation("Living with parents")
                .situationDetails("situation details")
                .situationDate("situation date")
                .keyDates("")
                .careAndContact("")
                .adoption("")
                .placementOrderApplication("")
                .placementCourt("Craigavon")
                .mothersName("Mandy Burns")
                .fathersName("Ted Burns")
                .fathersResponsibility("Yes")
                .socialWorkerName("Laura Wilson")
                .socialWorkerTel(TelephoneNumber.builder()
                    .telephoneNumber("02838882333")
                    .build())
                .additionalNeeds("Yes")
                .additionalNeedsDetails("additional needs details")
                .detailsHidden("Yes")
                .detailsHiddenReason("details hidden reason")
                .build())
            .build();
    }
}


