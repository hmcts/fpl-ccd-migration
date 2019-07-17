package uk.gov.hmcts.reform.migration.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;
import uk.gov.hmcts.reform.fpl.domain.CaseData;
import uk.gov.hmcts.reform.fpl.domain.OldRespondent;
import uk.gov.hmcts.reform.fpl.domain.OldRespondents;
import uk.gov.hmcts.reform.fpl.domain.Respondent;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.domain.common.RespondentParty;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RespondentsDataMigrationServiceTest {

    @Autowired
    private RespondentsDataMigrationService service;

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
    void whenOldRespondentStructureIsMigratedShouldReturnNewListStructure() {
        CaseData migratedCaseData = service.migrate(createOldRespondent(false, false));

        List<CollectionEntry<Respondent>> respondents = migratedCaseData.getRespondents1();

        RespondentParty firstRespondentParty = respondents.get(0).getValue().getParty();
        Respondent expectedRespondent = createNewRespondent();

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.getPartyID()).isInstanceOf(String.class);
        assertThat(firstRespondentParty.getPartyType()).isNull();
        assertThat(firstRespondentParty.getFirstName()).isEqualTo(expectedRespondent.getParty().getFirstName());
        assertThat(firstRespondentParty.getLastName()).isEqualTo(expectedRespondent.getParty().getLastName());
        assertThat(firstRespondentParty.getDateOfBirth()).isEqualTo(expectedRespondent.getParty().getDateOfBirth());
        assertThat(firstRespondentParty.getAddress()).isEqualTo(expectedRespondent.getParty().getAddress());
        assertThat(firstRespondentParty.getTelephoneNumber()).isEqualTo(expectedRespondent.getParty().getTelephoneNumber());
        assertThat(firstRespondentParty.getGender()).isEqualTo(expectedRespondent.getParty().getGender());
        assertThat(firstRespondentParty.getGenderIdentification()).isNull();
        assertThat(firstRespondentParty.getPlaceOfBirth()).isEqualTo(expectedRespondent.getParty().getPlaceOfBirth());
        assertThat(firstRespondentParty.getRelationshipToChild()).isEqualTo(expectedRespondent.getParty().getRelationshipToChild());
        assertThat(firstRespondentParty.getContactDetailsHidden()).isEqualTo(expectedRespondent.getParty().getContactDetailsHidden());
        assertThat(firstRespondentParty.getLitigationIssues()).isEqualTo(expectedRespondent.getParty().getLitigationIssues());
        assertThat(firstRespondentParty.getLitigationIssuesDetails()).isNull();
    }  
    
    @Test
    void whenOldRespondentsStructureIsMigratedShouldConvertManyRespondents() {
        CaseData migratedCaseData = service.migrate(createOldRespondent(true, false));

        List<CollectionEntry<Respondent>> respondents = migratedCaseData.getRespondents1();
        RespondentParty firstRespondentParty = respondents.get(0).getValue().getParty();
        RespondentParty otherRespondentParty = respondents.get(1).getValue().getParty();
        Respondent expectedRespondent = createNewRespondent();

        assertThat(respondents).hasSize(2);
        assertThat(firstRespondentParty.getPartyID()).isInstanceOf(String.class);
        assertThat(firstRespondentParty.getPartyType()).isNull();
        assertThat(firstRespondentParty.getFirstName()).isEqualTo(expectedRespondent.getParty().getFirstName());
        assertThat(firstRespondentParty.getLastName()).isEqualTo(expectedRespondent.getParty().getLastName());
        assertThat(firstRespondentParty.getDateOfBirth()).isEqualTo(expectedRespondent.getParty().getDateOfBirth());
        assertThat(firstRespondentParty.getAddress()).isEqualTo(expectedRespondent.getParty().getAddress());
        assertThat(firstRespondentParty.getTelephoneNumber()).isEqualTo(expectedRespondent.getParty().getTelephoneNumber());
        assertThat(firstRespondentParty.getGender()).isEqualTo(expectedRespondent.getParty().getGender());
        assertThat(firstRespondentParty.getGenderIdentification()).isNull();
        assertThat(firstRespondentParty.getPlaceOfBirth()).isEqualTo(expectedRespondent.getParty().getPlaceOfBirth());
        assertThat(firstRespondentParty.getRelationshipToChild()).isEqualTo(expectedRespondent.getParty().getRelationshipToChild());
        assertThat(firstRespondentParty.getContactDetailsHidden()).isEqualTo(expectedRespondent.getParty().getContactDetailsHidden());
        assertThat(firstRespondentParty.getLitigationIssues()).isEqualTo(expectedRespondent.getParty().getLitigationIssues());
        assertThat(firstRespondentParty.getLitigationIssuesDetails()).isNull();   assertThat(firstRespondentParty.getPartyID()).isInstanceOf(String.class);
        assertThat(otherRespondentParty.getPartyType()).isNull();
        assertThat(otherRespondentParty.getFirstName()).isEqualTo(expectedRespondent.getParty().getFirstName());
        assertThat(otherRespondentParty.getLastName()).isEqualTo(expectedRespondent.getParty().getLastName());
        assertThat(otherRespondentParty.getDateOfBirth()).isEqualTo(expectedRespondent.getParty().getDateOfBirth());
        assertThat(otherRespondentParty.getAddress()).isEqualTo(expectedRespondent.getParty().getAddress());
        assertThat(otherRespondentParty.getTelephoneNumber()).isEqualTo(expectedRespondent.getParty().getTelephoneNumber());
        assertThat(otherRespondentParty.getGender()).isEqualTo(expectedRespondent.getParty().getGender());
        assertThat(otherRespondentParty.getGenderIdentification()).isNull();
        assertThat(otherRespondentParty.getPlaceOfBirth()).isEqualTo(expectedRespondent.getParty().getPlaceOfBirth());
        assertThat(otherRespondentParty.getRelationshipToChild()).isEqualTo(expectedRespondent.getParty().getRelationshipToChild());
        assertThat(otherRespondentParty.getContactDetailsHidden()).isEqualTo(expectedRespondent.getParty().getContactDetailsHidden());
        assertThat(otherRespondentParty.getLitigationIssues()).isEqualTo(expectedRespondent.getParty().getLitigationIssues());
        assertThat(otherRespondentParty.getLitigationIssuesDetails()).isNull();
    }

    @Test
    void whenPartiallyFilledInOldRespondentStructureIsMigratedShouldReturnNewListStructureWithNullFields() {
        CaseData migratedCaseData = service.migrate(createOldRespondent(false, true));

        List<CollectionEntry<Respondent>> respondents = migratedCaseData.getRespondents1();

        RespondentParty firstRespondentParty = respondents.get(0).getValue().getParty();
        Respondent expectedRespondent = createNewRespondent();

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.getPartyType()).isNull();
        assertThat(firstRespondentParty.getFirstName()).isEqualTo(expectedRespondent.getParty().getFirstName());
        assertThat(firstRespondentParty.getLastName()).isEqualTo(expectedRespondent.getParty().getLastName());
        assertThat(firstRespondentParty.getDateOfBirth()).isNull();
        assertThat(firstRespondentParty.getAddress()).isEqualTo(Address.builder().build());
        assertThat(firstRespondentParty.getTelephoneNumber()).isNull();
        assertThat(firstRespondentParty.getEmail()).isNull();
        assertThat(firstRespondentParty.getGender()).isNull();
        assertThat(firstRespondentParty.getGenderIdentification()).isNull();
        assertThat(firstRespondentParty.getPlaceOfBirth()).isNull();
        assertThat(firstRespondentParty.getRelationshipToChild()).isNull();
        assertThat(firstRespondentParty.getContactDetailsHidden()).isNull();
        assertThat(firstRespondentParty.getLitigationIssues()).isNull();
        assertThat(firstRespondentParty.getLitigationIssuesDetails()).isNull();
    }

    @Test
    void whenPartiallyFilledInOldRespondentAddressIsMigratedShouldReturnNewListStructureWithNullAddressFields() {
        Map<String, Object> data = new HashMap<>();

        OldRespondent respondent = OldRespondent.builder()
            .address(Address.builder()
                .postcode("postcode")
                .county("Kent")
                .build())
            .build();

        data.put("respondents", OldRespondents.builder()
            .firstRespondent(respondent)
            .additional(ImmutableList.of())
            .build());

        CaseData migratedCaseData = service.migrate(data);

        List<CollectionEntry<Respondent>> respondents = migratedCaseData.getRespondents1();

        RespondentParty firstRespondentParty = respondents.get(0).getValue().getParty();

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.getPartyType()).isNull();
        assertThat(firstRespondentParty.getFirstName()).isNull();
        assertThat(firstRespondentParty.getLastName()).isNull();
        assertThat(firstRespondentParty.getDateOfBirth()).isNull();
        assertThat(firstRespondentParty.getAddress()).isEqualTo(Address.builder()
            .addressLine1(null)
            .addressLine2(null)
            .addressLine3(null)
            .postcode("postcode")
            .county("Kent")
            .country(null)
            .postTown(null)
            .build());
        assertThat(firstRespondentParty.getTelephoneNumber()).isNull();
        assertThat(firstRespondentParty.getEmail()).isNull();
        assertThat(firstRespondentParty.getGender()).isNull();
        assertThat(firstRespondentParty.getGenderIdentification()).isNull();
        assertThat(firstRespondentParty.getPlaceOfBirth()).isNull();
        assertThat(firstRespondentParty.getRelationshipToChild()).isNull();
        assertThat(firstRespondentParty.getContactDetailsHidden()).isNull();
        assertThat(firstRespondentParty.getLitigationIssues()).isNull();
        assertThat(firstRespondentParty.getLitigationIssuesDetails()).isNull();
    }

    @Test
    void whenOldRespondentWithOneNameIsMigratedShouldReturnNewListStructureWithOnlyFirstName() {
        Map<String, Object> data = new HashMap<>();

        OldRespondent respondent = OldRespondent.builder()
            .name("Beyoncé")
            .build();

        data.put("respondents", OldRespondents.builder()
            .firstRespondent(respondent)
            .additional(ImmutableList.of())
            .build());

        CaseData migratedCaseData = service.migrate(data);

        List<CollectionEntry<Respondent>> respondents = migratedCaseData.getRespondents1();

        RespondentParty firstRespondentParty = respondents.get(0).getValue().getParty();

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.getFirstName()).isEqualTo("Beyoncé");
        assertThat(firstRespondentParty.getLastName()).isNull();
    }

    @Test
    void whenOldRespondentWithManyNamesIsMigratedShouldReturnNewListStructureWithMultipleFirstNames() {
        Map<String, Object> data = new HashMap<>();

        OldRespondent respondent = OldRespondent.builder()
            .name("Jean Paul Gautier")
            .build();

        data.put("respondents", OldRespondents.builder()
            .firstRespondent(respondent)
            .additional(ImmutableList.of())
            .build());

        CaseData migratedCaseData = service.migrate(data);

        List<CollectionEntry<Respondent>> respondents = migratedCaseData.getRespondents1();

        RespondentParty firstRespondentParty = respondents.get(0).getValue().getParty();

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.getFirstName()).isEqualTo("Jean Paul");
        assertThat(firstRespondentParty.getLastName()).isEqualTo("Gautier");
    }

    private Map<String, Object> createOldRespondent(boolean multiple, boolean partial) {
        Map<String, Object> data = new HashMap<>();
        OldRespondent respondent;

        if (partial) {
            respondent = OldRespondent.builder()
                .name("An Other")
                .build();
        } else {
            respondent = OldRespondent.builder()
                .name("An Other")
                .dob("1999-12-31")
                .address(Address.builder()
                    .addressLine1("Building number")
                    .addressLine2("Street Name")
                    .addressLine3("Other street Name")
                    .country("Country")
                    .county("County")
                    .postcode("AA1 1AA")
                    .postTown("Town")
                    .build())
                .contactDetailsHidden("No")
                .gender("Male")
                .litigationIssues("No")
                .placeOfBirth("Town")
                .relationshipToChild("Father")
                .telephone("01482000000")
                .build();
        }

        if (multiple) {
            respondent = OldRespondent.builder()
                .name("An Other")
                .dob("1999-12-31")
                .address(Address.builder()
                    .addressLine1("Building number")
                    .addressLine2("Street Name")
                    .addressLine3("Other street Name")
                    .country("Country")
                    .county("County")
                    .postcode("AA1 1AA")
                    .postTown("Town")
                    .build())
                .contactDetailsHidden("No")
                .gender("Male")
                .litigationIssues("No")
                .placeOfBirth("Town")
                .relationshipToChild("Father")
                .telephone("01482000000")
                .build();

            data.put("respondents", OldRespondents.builder()
                .firstRespondent(respondent)
                .additional(ImmutableList.of(CollectionEntry.<OldRespondent>builder()
                        .id("")
                        .value(respondent)
                        .build()))
                .build());
        } else {
            data.put("respondents", OldRespondents.builder()
                .firstRespondent(respondent)
                .additional(ImmutableList.of())
                .build());
        }

        return data;
    }

    private Respondent createNewRespondent() {
        return Respondent.builder()
            .party(RespondentParty.builder()
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
                .contactDetailsHidden("No")
                .gender("Male")
                .genderIdentification("")
                .litigationIssues("No")
                .litigationIssuesDetails("")
                .placeOfBirth("Town")
                .relationshipToChild("Father")
                .dateOfBirth("1999-12-31")
                .telephoneNumber(TelephoneNumber.builder()
                    .telephoneNumber("01482000000")
                    .build())
                .email(EmailAddress.builder()
                    .email("")
                    .build())
                .build())
            .leadRespondentIndicator("")
            .build();
    }
}
