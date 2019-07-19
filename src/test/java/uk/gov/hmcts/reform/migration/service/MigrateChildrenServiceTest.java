package uk.gov.hmcts.reform.migration.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;
import uk.gov.hmcts.reform.fpl.domain.CaseData;
import uk.gov.hmcts.reform.fpl.domain.OldChild;
import uk.gov.hmcts.reform.fpl.domain.OldChildren;
import uk.gov.hmcts.reform.fpl.domain.Child;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;
import uk.gov.hmcts.reform.fpl.domain.common.ChildParty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MigrateChildrenServiceTest {

    @Autowired
    private ChildrenDataMigrationService service;

    @Test
    void whenOldStructureExistsAcceptsShouldReturnTrue() {
        Map<String, Object> data = new HashMap<>();

        OldChild child = OldChild.builder().build();

        data.put("children", child);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        assertThat(service.accepts().test(caseDetails)).isEqualTo(true);
    }

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
        CaseData migratedCaseData = service.migrate(createOldChild(false, false));

        List<CollectionEntry<Child>> children = migratedCaseData.getChildren1();

        ChildParty firstChildParty = children.get(0).getValue().getParty();
        Child expectedChild = createNewChild();

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.getPartyID()).isInstanceOf(String.class);
        assertThat(firstChildParty.getPartyType()).isNull();
        assertThat(firstChildParty.getFirstName()).isEqualTo(expectedChild.getParty().getFirstName());
        assertThat(firstChildParty.getLastName()).isEqualTo(expectedChild.getParty().getLastName());
        assertThat(firstChildParty.getDateOfBirth()).isEqualTo(expectedChild.getParty().getDateOfBirth());
        assertThat(firstChildParty.getAddress()).isEqualTo(expectedChild.getParty().getAddress());
        assertThat(firstChildParty.getGender()).isEqualTo(expectedChild.getParty().getGender());
        assertThat(firstChildParty.getGenderIdentification()).isEqualTo(expectedChild.getParty().getGenderIdentification());
        assertThat(firstChildParty.getLitigationIssues()).isEqualTo(expectedChild.getParty().getLitigationIssues());
        assertThat(firstChildParty.getLitigationIssuesDetails()).isEqualTo(expectedChild.getParty().getLitigationIssuesDetails());
        assertThat(firstChildParty.getLivingSituation()).isEqualTo(expectedChild.getParty().getLivingSituation());
        assertThat(firstChildParty.getSituationDetails()).isEqualTo(expectedChild.getParty().getSituationDetails());
        assertThat(firstChildParty.getSituationDate()).isEqualTo(expectedChild.getParty().getSituationDate());
        assertThat(firstChildParty.getKeyDates()).isEqualTo(expectedChild.getParty().getKeyDates());
        assertThat(firstChildParty.getCareAndContact()).isEqualTo(expectedChild.getParty().getCareAndContact());
        assertThat(firstChildParty.getAdoption()).isEqualTo(expectedChild.getParty().getAdoption());
        assertThat(firstChildParty.getPlacementOrderApplication()).isEqualTo(expectedChild.getParty().getPlacementOrderApplication());
        assertThat(firstChildParty.getPlacementCourt()).isEqualTo(expectedChild.getParty().getPlacementCourt());
        assertThat(firstChildParty.getMothersName()).isEqualTo(expectedChild.getParty().getMothersName());
        assertThat(firstChildParty.getFathersName()).isEqualTo(expectedChild.getParty().getFathersName());
        assertThat(firstChildParty.getFathersResponsibility()).isEqualTo(expectedChild.getParty().getFathersResponsibility());
        assertThat(firstChildParty.getSocialWorkerName()).isEqualTo(expectedChild.getParty().getSocialWorkerName());
        assertThat(firstChildParty.getSocialWorkerTel()).isEqualTo(expectedChild.getParty().getSocialWorkerTel());
        assertThat(firstChildParty.getAdditionalNeeds()).isEqualTo(expectedChild.getParty().getAdditionalNeeds());
        assertThat(firstChildParty.getAdditionalNeedsDetails()).isEqualTo(expectedChild.getParty().getAdditionalNeedsDetails());
        assertThat(firstChildParty.getDetailsHidden()).isEqualTo(expectedChild.getParty().getDetailsHidden());
        assertThat(firstChildParty.getDetailsHiddenReason()).isEqualTo(expectedChild.getParty().getDetailsHiddenReason());
    }

    @Test
    void whenOldChildrenStructureIsMigratedShouldConvertManyChildren() {
        CaseData migratedCaseData = service.migrate(createOldChild(true, false));

        List<CollectionEntry<Child>> children = migratedCaseData.getChildren1();
        ChildParty firstChildParty = children.get(0).getValue().getParty();
        ChildParty OtherChildParty = children.get(1).getValue().getParty();
        Child expectedChild = createNewChild();

        assertThat(children).hasSize(2);
        assertThat(firstChildParty.getPartyID()).isInstanceOf(String.class);
        assertThat(firstChildParty.getPartyType()).isNull();
        assertThat(firstChildParty.getFirstName()).isEqualTo(expectedChild.getParty().getFirstName());
        assertThat(firstChildParty.getLastName()).isEqualTo(expectedChild.getParty().getLastName());
        assertThat(firstChildParty.getDateOfBirth()).isEqualTo(expectedChild.getParty().getDateOfBirth());
        assertThat(firstChildParty.getAddress()).isEqualTo(expectedChild.getParty().getAddress());
        assertThat(firstChildParty.getGender()).isEqualTo(expectedChild.getParty().getGender());
        assertThat(firstChildParty.getGenderIdentification()).isEqualTo(expectedChild.getParty().getGenderIdentification());
        assertThat(firstChildParty.getLitigationIssues()).isEqualTo(expectedChild.getParty().getLitigationIssues());
        assertThat(firstChildParty.getLitigationIssuesDetails()).isEqualTo(expectedChild.getParty().getLitigationIssuesDetails());
        assertThat(firstChildParty.getLivingSituation()).isEqualTo(expectedChild.getParty().getLivingSituation());
        assertThat(firstChildParty.getSituationDetails()).isEqualTo(expectedChild.getParty().getSituationDetails());
        assertThat(firstChildParty.getSituationDate()).isEqualTo(expectedChild.getParty().getSituationDate());
        assertThat(firstChildParty.getKeyDates()).isEqualTo(expectedChild.getParty().getKeyDates());
        assertThat(firstChildParty.getCareAndContact()).isEqualTo(expectedChild.getParty().getCareAndContact());
        assertThat(firstChildParty.getAdoption()).isEqualTo(expectedChild.getParty().getAdoption());
        assertThat(firstChildParty.getPlacementOrderApplication()).isEqualTo(expectedChild.getParty().getPlacementOrderApplication());
        assertThat(firstChildParty.getPlacementCourt()).isEqualTo(expectedChild.getParty().getPlacementCourt());
        assertThat(firstChildParty.getMothersName()).isEqualTo(expectedChild.getParty().getMothersName());
        assertThat(firstChildParty.getFathersName()).isEqualTo(expectedChild.getParty().getFathersName());
        assertThat(firstChildParty.getFathersResponsibility()).isEqualTo(expectedChild.getParty().getFathersResponsibility());
        assertThat(firstChildParty.getSocialWorkerName()).isEqualTo(expectedChild.getParty().getSocialWorkerName());
        assertThat(firstChildParty.getSocialWorkerTel()).isEqualTo(expectedChild.getParty().getSocialWorkerTel());
        assertThat(firstChildParty.getAdditionalNeeds()).isEqualTo(expectedChild.getParty().getAdditionalNeeds());
        assertThat(firstChildParty.getAdditionalNeedsDetails()).isEqualTo(expectedChild.getParty().getAdditionalNeedsDetails());
        assertThat(firstChildParty.getDetailsHidden()).isEqualTo(expectedChild.getParty().getDetailsHidden());
        assertThat(firstChildParty.getDetailsHiddenReason()).isEqualTo(expectedChild.getParty().getDetailsHiddenReason());

        assertThat(OtherChildParty.getPartyID()).isInstanceOf(String.class);
        assertThat(OtherChildParty.getPartyType()).isNull();
        assertThat(OtherChildParty.getFirstName()).isEqualTo(expectedChild.getParty().getFirstName());
        assertThat(OtherChildParty.getLastName()).isEqualTo(expectedChild.getParty().getLastName());
        assertThat(OtherChildParty.getDateOfBirth()).isEqualTo(expectedChild.getParty().getDateOfBirth());
        assertThat(OtherChildParty.getAddress()).isEqualTo(expectedChild.getParty().getAddress());
        assertThat(OtherChildParty.getGender()).isEqualTo(expectedChild.getParty().getGender());
        assertThat(OtherChildParty.getGenderIdentification()).isEqualTo(expectedChild.getParty().getGenderIdentification());
        assertThat(OtherChildParty.getLitigationIssues()).isEqualTo(expectedChild.getParty().getLitigationIssues());
        assertThat(OtherChildParty.getLitigationIssuesDetails()).isEqualTo(expectedChild.getParty().getLitigationIssuesDetails());
        assertThat(OtherChildParty.getLivingSituation()).isEqualTo(expectedChild.getParty().getLivingSituation());
        assertThat(OtherChildParty.getSituationDetails()).isEqualTo(expectedChild.getParty().getSituationDetails());
        assertThat(OtherChildParty.getSituationDate()).isEqualTo(expectedChild.getParty().getSituationDate());
        assertThat(OtherChildParty.getKeyDates()).isEqualTo(expectedChild.getParty().getKeyDates());
        assertThat(OtherChildParty.getCareAndContact()).isEqualTo(expectedChild.getParty().getCareAndContact());
        assertThat(OtherChildParty.getAdoption()).isEqualTo(expectedChild.getParty().getAdoption());
        assertThat(OtherChildParty.getPlacementOrderApplication()).isEqualTo(expectedChild.getParty().getPlacementOrderApplication());
        assertThat(OtherChildParty.getPlacementCourt()).isEqualTo(expectedChild.getParty().getPlacementCourt());
        assertThat(OtherChildParty.getMothersName()).isEqualTo(expectedChild.getParty().getMothersName());
        assertThat(OtherChildParty.getFathersName()).isEqualTo(expectedChild.getParty().getFathersName());
        assertThat(OtherChildParty.getFathersResponsibility()).isEqualTo(expectedChild.getParty().getFathersResponsibility());
        assertThat(OtherChildParty.getSocialWorkerName()).isEqualTo(expectedChild.getParty().getSocialWorkerName());
        assertThat(OtherChildParty.getSocialWorkerTel()).isEqualTo(expectedChild.getParty().getSocialWorkerTel());
        assertThat(OtherChildParty.getAdditionalNeeds()).isEqualTo(expectedChild.getParty().getAdditionalNeeds());
        assertThat(OtherChildParty.getAdditionalNeedsDetails()).isEqualTo(expectedChild.getParty().getAdditionalNeedsDetails());
        assertThat(OtherChildParty.getDetailsHidden()).isEqualTo(expectedChild.getParty().getDetailsHidden());
        assertThat(OtherChildParty.getDetailsHiddenReason()).isEqualTo(expectedChild.getParty().getDetailsHiddenReason());
    }

    @Test
    void whenPartiallyFilledInOldChildrenStructureIsMigratedShouldReturnNewListStructureWithNullFields() {
        CaseData migratedCaseData = service.migrate(createOldChild(false, true));

        List<CollectionEntry<Child>> children = migratedCaseData.getChildren1();

        ChildParty firstChildParty = children.get(0).getValue().getParty();
        Child expectedChild = createNewChild();

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.getPartyType()).isNull();
        assertThat(firstChildParty.getFirstName()).isEqualTo(expectedChild.getParty().getFirstName());
        assertThat(firstChildParty.getLastName()).isEqualTo(expectedChild.getParty().getLastName());
        assertThat(firstChildParty.getDateOfBirth()).isNull();
        assertThat(firstChildParty.getAddress()).isEqualTo(Address.builder().build());
        assertThat(firstChildParty.getGender()).isNull();
        assertThat(firstChildParty.getGenderIdentification()).isNull();
        assertThat(firstChildParty.getLitigationIssues()).isNull();
        assertThat(firstChildParty.getLitigationIssuesDetails()).isNull();
        assertThat(firstChildParty.getLivingSituation()).isNull();
        assertThat(firstChildParty.getSituationDetails()).isNull();
        assertThat(firstChildParty.getSituationDate()).isNull();
        assertThat(firstChildParty.getKeyDates()).isNull();
        assertThat(firstChildParty.getCareAndContact()).isNull();
        assertThat(firstChildParty.getAdoption()).isNull();
        assertThat(firstChildParty.getPlacementOrderApplication()).isNull();
        assertThat(firstChildParty.getPlacementCourt()).isNull();
        assertThat(firstChildParty.getMothersName()).isNull();
        assertThat(firstChildParty.getFathersName()).isNull();
        assertThat(firstChildParty.getFathersResponsibility()).isNull();
        assertThat(firstChildParty.getSocialWorkerName()).isNull();
        assertThat(firstChildParty.getSocialWorkerTel()).isNull();
        assertThat(firstChildParty.getAdditionalNeeds()).isNull();
        assertThat(firstChildParty.getAdditionalNeedsDetails()).isNull();
        assertThat(firstChildParty.getDetailsHidden()).isNull();
        assertThat(firstChildParty.getDetailsHiddenReason()).isNull();
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
            .additionalChildren(ImmutableList.of())
            .build());

        CaseData migratedCaseData = service.migrate(data);

        List<CollectionEntry<Child>> children = migratedCaseData.getChildren1();
        ChildParty firstChildParty = children.get(0).getValue().getParty();

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.getPartyType()).isNull();
        assertThat(firstChildParty.getFirstName()).isNull();
        assertThat(firstChildParty.getLastName()).isNull();
        assertThat(firstChildParty.getDateOfBirth()).isNull();
        assertThat(firstChildParty.getAddress()).isEqualTo(Address.builder()
            .addressLine1(null)
            .addressLine2(null)
            .addressLine3(null)
            .postcode("postcode")
            .county("Kent")
            .country(null)
            .postTown(null)
            .build());
        assertThat(firstChildParty.getGender()).isNull();
        assertThat(firstChildParty.getGenderIdentification()).isNull();
        assertThat(firstChildParty.getLitigationIssues()).isNull();
        assertThat(firstChildParty.getLitigationIssuesDetails()).isNull();
        assertThat(firstChildParty.getLivingSituation()).isNull();
        assertThat(firstChildParty.getSituationDetails()).isNull();
        assertThat(firstChildParty.getSituationDate()).isNull();
        assertThat(firstChildParty.getKeyDates()).isNull();
        assertThat(firstChildParty.getCareAndContact()).isNull();
        assertThat(firstChildParty.getAdoption()).isNull();
        assertThat(firstChildParty.getPlacementOrderApplication()).isNull();
        assertThat(firstChildParty.getPlacementCourt()).isNull();
        assertThat(firstChildParty.getMothersName()).isNull();
        assertThat(firstChildParty.getFathersName()).isNull();
        assertThat(firstChildParty.getFathersResponsibility()).isNull();
        assertThat(firstChildParty.getSocialWorkerName()).isNull();
        assertThat(firstChildParty.getSocialWorkerTel()).isNull();
        assertThat(firstChildParty.getAdditionalNeeds()).isNull();
        assertThat(firstChildParty.getAdditionalNeedsDetails()).isNull();
        assertThat(firstChildParty.getDetailsHidden()).isNull();
        assertThat(firstChildParty.getDetailsHiddenReason()).isNull();
    }

    @Test
    void whenOldChildWithOneNameIsMigratedShouldReturnNewListStructureWithOnlyFirstName() {
        Map<String, Object> data = new HashMap<>();

        OldChild child = OldChild.builder()
            .childName("Sansa")
            .build();

        data.put("children", OldChildren.builder()
            .firstChild(child)
            .additionalChildren(ImmutableList.of())
            .build());

        CaseData migratedCaseData = service.migrate(data);

        List<CollectionEntry<Child>> children = migratedCaseData.getChildren1();

        ChildParty firstChildParty = children.get(0).getValue().getParty();

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.getFirstName()).isEqualTo("Sansa");
        assertThat(firstChildParty.getLastName()).isNull();
    }

    @Test
    void whenOldChildtWithManyNamesIsMigratedShouldReturnNewListStructureWithMultipleFirstNames() {
        Map<String, Object> data = new HashMap<>();

        OldChild child = OldChild.builder()
            .childName("Sacha Baron Cohen")
            .build();

        data.put("children", OldChildren.builder()
            .firstChild(child)
            .additionalChildren(ImmutableList.of())
            .build());

        CaseData migratedCaseData = service.migrate(data);

        List<CollectionEntry<Child>> children = migratedCaseData.getChildren1();

        ChildParty firstChildParty = children.get(0).getValue().getParty();

        assertThat(children).hasSize(1);
        assertThat(firstChildParty.getFirstName()).isEqualTo("Sacha Baron");
        assertThat(firstChildParty.getLastName()).isEqualTo("Cohen");
    }

    private Map<String, Object> createOldChild(boolean multiple, boolean partial) {
        Map<String, Object> data = new HashMap<>();
        OldChild child;

        if (partial) {
            child = OldChild.builder()
                .childName("An Other")
                .build();
        } else {
            child = OldChild.builder()
                .childName("An Other")
                .childDOB("1999-12-31")
                .childGender("Unknown")
                .childGenderIdentification("Boy")
                .livingSituation("Living with parents")
                .situationDetails("Situation details")
                .situationDate("1999-12-31")
                .keyDates("1999-12-31")
                .careAndContact("Care and contact")
                .adoption("Yes")
                .placementOrderApplication("Yes")
                .placementCourt("Craigavon")
                .mothersName("Mandy Burns")
                .fathersName("Ted Burns")
                .fathersResponsibility("Yes")
                .socialWorkerName("Laura Wilson")
                .socialWorkerTel("02838882333")
                .additionalNeeds("Yes")
                .additionalNeedsDetails("Details")
                .detailsHidden("Yes")
                .detailsHiddenReason("Details reason")
                .litigationIssues("Yes")
                .litigationIssuesDetails("Litigation issue details")
                .address(Address.builder()
                    .addressLine1("Building number")
                    .addressLine2("Street Name")
                    .addressLine3("Other street Name")
                    .county("County")
                    .postTown("Town")
                    .postcode("AA1 1AA")
                    .country("Country")
                    .build()
                ).build();
        }

        if (multiple) {
            child = OldChild.builder()
                .childName("An Other")
                .childDOB("1999-12-31")
                .childGender("Unknown")
                .childGenderIdentification("Boy")
                .livingSituation("Living with parents")
                .situationDetails("Situation details")
                .situationDate("1999-12-31")
                .keyDates("1999-12-31")
                .careAndContact("Care and contact")
                .adoption("Yes")
                .placementOrderApplication("Yes")
                .placementCourt("Craigavon")
                .mothersName("Mandy Burns")
                .fathersName("Ted Burns")
                .fathersResponsibility("Yes")
                .socialWorkerName("Laura Wilson")
                .socialWorkerTel("02838882333")
                .additionalNeeds("Yes")
                .additionalNeedsDetails("Details")
                .detailsHidden("Yes")
                .detailsHiddenReason("Details reason")
                .litigationIssues("Yes")
                .litigationIssuesDetails("Litigation issue details")
                .address(Address.builder()
                    .addressLine1("Building number")
                    .addressLine2("Street Name")
                    .addressLine3("Other street Name")
                    .county("County")
                    .postTown("Town")
                    .postcode("AA1 1AA")
                    .country("Country")
                    .build()
                ).build();

            data.put("children", OldChildren.builder()
                .firstChild(child)
                .additionalChildren(ImmutableList.of(CollectionEntry.<OldChild>builder()
                    .id("")
                    .value(child)
                    .build()))
                .build());
        } else {
            data.put("children", OldChildren.builder()
                .firstChild(child)
                .additionalChildren(ImmutableList.of())
                .build());
        }

        return data;
    }

    private Child createNewChild() {
        return Child.builder()
            .party(ChildParty.builder()
                .partyID("")
                .firstName("An")
                .lastName("Other")
                .address(Address.builder()
                    .addressLine1("Building number")
                    .addressLine2("Street Name")
                    .addressLine3("Other street Name")
                    .country("Country")
                    .county("County")
                    .postcode("AA1 1AA")
                    .postTown("Town")
                    .build())
                .gender("Unknown")
                .genderIdentification("Boy")
                .litigationIssues("Yes")
                .litigationIssuesDetails("Litigation issue details")
                .dateOfBirth("1999-12-31")
                .livingSituation("Living with parents")
                .situationDetails("Situation details")
                .situationDate("1999-12-31")
                .keyDates("1999-12-31")
                .careAndContact("Care and contact")
                .adoption("Yes")
                .placementOrderApplication("Yes")
                .placementCourt("Craigavon")
                .mothersName("Mandy Burns")
                .fathersName("Ted Burns")
                .fathersResponsibility("Yes")
                .socialWorkerName("Laura Wilson")
                .socialWorkerTel(TelephoneNumber.builder()
                    .telephoneNumber("02838882333")
                    .build())
                .additionalNeeds("Yes")
                .additionalNeedsDetails("Details")
                .detailsHidden("Yes")
                .detailsHiddenReason("Details reason")
                .email(EmailAddress.builder()
                    .email("")
                    .build())
                .build())
            .build();
    }
}


