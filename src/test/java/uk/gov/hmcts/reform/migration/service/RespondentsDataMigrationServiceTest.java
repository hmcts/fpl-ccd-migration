package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

class RespondentsDataMigrationServiceTest {

    private RespondentsDataMigrationService service = new RespondentsDataMigrationService();
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
    void whenOldRespondentStructureIsMigratedShouldReturnNewListStructure() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(createOldRespondent(false, false))
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) caseDetails.getData().get("respondents1");

        Map<String, Object> firstRespondent = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> firstRespondentParty = (Map<String, Object>) firstRespondent.get("party");
        Respondent expectedRespondent = createNewRespondent();

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.get("partyID")).isInstanceOf(String.class);
        assertThat(firstRespondentParty.get("partyType")).isNull();
        assertThat(firstRespondentParty.get("firstName")).isEqualTo(expectedRespondent.getParty().getFirstName());
        assertThat(firstRespondentParty.get("lastName")).isEqualTo(expectedRespondent.getParty().getLastName());
        assertThat(firstRespondentParty.get("dateOfBirth")).isEqualTo(expectedRespondent.getParty().getDateOfBirth());
        assertThat(objectMapper.convertValue(firstRespondentParty.get("address"), Address.class)).isEqualTo(expectedRespondent.getParty().getAddress());
        assertThat(firstRespondentParty.get("telephoneNumber")).isEqualTo(objectMapper.convertValue(expectedRespondent.getParty().getTelephoneNumber(), Object.class));
        assertThat(firstRespondentParty.get("gender")).isEqualTo(expectedRespondent.getParty().getGender());
        assertThat(firstRespondentParty.get("genderIdentification")).isNull();
        assertThat(firstRespondentParty.get("placeOfBirth")).isEqualTo(expectedRespondent.getParty().getPlaceOfBirth());
        assertThat(firstRespondentParty.get("relationshipToChild")).isEqualTo(expectedRespondent.getParty().getRelationshipToChild());
        assertThat(firstRespondentParty.get("contactDetailsHidden")).isEqualTo(expectedRespondent.getParty().getContactDetailsHidden());
        assertThat(firstRespondentParty.get("litigationIssues")).isEqualTo(expectedRespondent.getParty().getLitigationIssues());
        assertThat(firstRespondentParty.get("litigationIssuesDetails")).isNull();
    }  
    
    @Test
    void whenOldRespondentsStructureIsMigratedShouldConvertManyRespondents() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(createOldRespondent(true, false))
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) caseDetails.getData().get("respondents1");

        Map<String, Object> firstRespondent = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> otherRespondent = (Map<String, Object>) respondents.get(1).get("value");
        Map<String, Object> firstRespondentParty = (Map<String, Object>) firstRespondent.get("party");
        Map<String, Object> otherRespondentParty = (Map<String, Object>) otherRespondent.get("party");
        Respondent expectedRespondent = createNewRespondent();

        assertThat(respondents).hasSize(2);
        assertThat(firstRespondentParty.get("partyID")).isInstanceOf(String.class);
        assertThat(firstRespondentParty.get("partyType")).isNull();
        assertThat(firstRespondentParty.get("firstName")).isEqualTo(expectedRespondent.getParty().getFirstName());
        assertThat(firstRespondentParty.get("lastName")).isEqualTo(expectedRespondent.getParty().getLastName());
        assertThat(firstRespondentParty.get("dateOfBirth")).isEqualTo(expectedRespondent.getParty().getDateOfBirth());
        assertThat(objectMapper.convertValue(firstRespondentParty.get("address"), Address.class)).isEqualTo(expectedRespondent.getParty().getAddress());
        assertThat(firstRespondentParty.get("telephoneNumber")).isEqualTo(objectMapper.convertValue(expectedRespondent.getParty().getTelephoneNumber(), Object.class));
        assertThat(firstRespondentParty.get("gender")).isEqualTo(expectedRespondent.getParty().getGender());
        assertThat(firstRespondentParty.get("genderIdentification")).isNull();
        assertThat(firstRespondentParty.get("placeOfBirth")).isEqualTo(expectedRespondent.getParty().getPlaceOfBirth());
        assertThat(firstRespondentParty.get("relationshipToChild")).isEqualTo(expectedRespondent.getParty().getRelationshipToChild());
        assertThat(firstRespondentParty.get("contactDetailsHidden")).isEqualTo(expectedRespondent.getParty().getContactDetailsHidden());
        assertThat(firstRespondentParty.get("litigationIssues")).isEqualTo(expectedRespondent.getParty().getLitigationIssues());
        assertThat(firstRespondentParty.get("litigationIssuesDetails")).isNull();   assertThat(firstRespondentParty.get("partyID")).isInstanceOf(String.class);
        assertThat(otherRespondentParty.get("partyType")).isNull();
        assertThat(otherRespondentParty.get("firstName")).isEqualTo(expectedRespondent.getParty().getFirstName());
        assertThat(otherRespondentParty.get("lastName")).isEqualTo(expectedRespondent.getParty().getLastName());
        assertThat(otherRespondentParty.get("dateOfBirth")).isEqualTo(expectedRespondent.getParty().getDateOfBirth());
        assertThat(objectMapper.convertValue(otherRespondentParty.get("address"), Address.class)).isEqualTo(expectedRespondent.getParty().getAddress());
        assertThat(otherRespondentParty.get("telephoneNumber")).isEqualTo(objectMapper.convertValue(expectedRespondent.getParty().getTelephoneNumber(), Object.class));
        assertThat(otherRespondentParty.get("gender")).isEqualTo(expectedRespondent.getParty().getGender());
        assertThat(otherRespondentParty.get("genderIdentification")).isNull();
        assertThat(otherRespondentParty.get("placeOfBirth")).isEqualTo(expectedRespondent.getParty().getPlaceOfBirth());
        assertThat(otherRespondentParty.get("relationshipToChild")).isEqualTo(expectedRespondent.getParty().getRelationshipToChild());
        assertThat(otherRespondentParty.get("contactDetailsHidden")).isEqualTo(expectedRespondent.getParty().getContactDetailsHidden());
        assertThat(otherRespondentParty.get("litigationIssues")).isEqualTo(expectedRespondent.getParty().getLitigationIssues());
        assertThat(otherRespondentParty.get("litigationIssuesDetails")).isNull();
    }

    @Test
    void whenPartiallyFilledInOldRespondentStructureIsMigratedShouldReturnNewListStructureWithNullFields() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(createOldRespondent(false, true))
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) caseDetails.getData().get("respondents1");

        Map<String, Object> firstRespondent = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> firstRespondentParty = (Map<String, Object>) firstRespondent.get("party");
        Respondent expectedRespondent = createNewRespondent();

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.get("partyType")).isNull();
        assertThat(firstRespondentParty.get("firstName")).isEqualTo(expectedRespondent.getParty().getFirstName());
        assertThat(firstRespondentParty.get("lastName")).isEqualTo(expectedRespondent.getParty().getLastName());
        assertThat(firstRespondentParty.get("dateOfBirth")).isNull();
        assertThat(objectMapper.convertValue(firstRespondentParty.get("address"), Address.class)).isEqualTo(Address.builder().build());
        assertThat(firstRespondentParty.get("telephoneNumber")).isNull();
        assertThat(firstRespondentParty.get("email")).isNull();
        assertThat(firstRespondentParty.get("gender")).isNull();
        assertThat(firstRespondentParty.get("genderIdentification")).isNull();
        assertThat(firstRespondentParty.get("placeOfBirth")).isNull();
        assertThat(firstRespondentParty.get("relationshipToChild")).isNull();
        assertThat(firstRespondentParty.get("contactDetailsHidden")).isNull();
        assertThat(firstRespondentParty.get("litigationIssues")).isNull();
        assertThat(firstRespondentParty.get("litigationIssuesDetails")).isNull();
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
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) caseDetails.getData().get("respondents1");

        Map<String, Object> firstRespondent = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> firstRespondentParty = (Map<String, Object>) firstRespondent.get("party");

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.get("partyType")).isNull();
        assertThat(firstRespondentParty.get("firstName")).isNull();
        assertThat(firstRespondentParty.get("lastName")).isNull();
        assertThat(firstRespondentParty.get("dateOfBirth")).isNull();
        assertThat(objectMapper.convertValue(firstRespondentParty.get("address"), Address.class)).isEqualTo(Address.builder()
            .addressLine1(null)
            .addressLine2(null)
            .addressLine3(null)
            .postcode("postcode")
            .county("Kent")
            .country(null)
            .postTown(null)
            .build());
        assertThat(firstRespondentParty.get("telephoneNumber")).isNull();
        assertThat(firstRespondentParty.get("email")).isNull();
        assertThat(firstRespondentParty.get("gender")).isNull();
        assertThat(firstRespondentParty.get("genderIdentification")).isNull();
        assertThat(firstRespondentParty.get("placeOfBirth")).isNull();
        assertThat(firstRespondentParty.get("relationshipToChild")).isNull();
        assertThat(firstRespondentParty.get("contactDetailsHidden")).isNull();
        assertThat(firstRespondentParty.get("litigationIssues")).isNull();
        assertThat(firstRespondentParty.get("litigationIssuesDetails")).isNull();
    }

    @Test
    void whenOldRespondentWithOneNameIsMigratedShouldReturnNewListStructureWithOnlyFirstName() {
        Map<String, Object> data = new HashMap<>();

        OldRespondent respondent = OldRespondent.builder()
            .name("Beyoncé")
            .build();

        data.put("respondents", OldRespondents.builder()
            .firstRespondent(respondent)
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) caseDetails.getData().get("respondents1");

        Map<String, Object> firstRespondent = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> firstRespondentParty = (Map<String, Object>) firstRespondent.get("party");

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.get("firstName")).isEqualTo("Beyoncé");
        assertThat(firstRespondentParty.get("lastName")).isNull();
    }

    @Test
    void whenOldRespondentWithManyNamesIsMigratedShouldReturnNewListStructureWithMultipleFirstNames() {
        Map<String, Object> data = new HashMap<>();

        OldRespondent respondent = OldRespondent.builder()
            .name("Jean Paul Gautier")
            .build();

        data.put("respondents", OldRespondents.builder()
            .firstRespondent(respondent)
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) caseDetails.getData().get("respondents1");

        Map<String, Object> firstRespondent = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> firstRespondentParty = (Map<String, Object>) firstRespondent.get("party");

        assertThat(respondents).hasSize(1);
        assertThat(firstRespondentParty.get("firstName")).isEqualTo("Jean Paul");
        assertThat(firstRespondentParty.get("lastName")).isEqualTo("Gautier");
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
                .additional(ImmutableList.of(
                    ImmutableMap.of(
                        "id", "",
                        "value", respondent)))
                .build());
        } else {
            data.put("respondents", OldRespondents.builder()
                .firstRespondent(respondent)
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
