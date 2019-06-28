package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.OldRespondent;
import uk.gov.hmcts.reform.domain.OldRespondents;
import uk.gov.hmcts.reform.domain.Respondent;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
import uk.gov.hmcts.reform.domain.common.Party;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RespondentsDataMigrationServiceTest {

    private RespondentsDataMigrationService service = new RespondentsDataMigrationService();
    private ObjectMapper objectMapper = new ObjectMapper();

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

    @SuppressWarnings({"unchecked", "LineLength"})
    @Test
    public void whenOldRespondentStructureIsMigratedShouldReturnNewListStructure() {
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
    public void whenPartiallyFilledInOldRespondentStructureIsMigratedShouldReturnNewListStructureWithNullFields() {
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
        assertThat(firstRespondentParty.get("telephoneNumber")).isEqualTo(objectMapper.convertValue(TelephoneNumber.builder().build(), Object.class));
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
    public void whenPartiallyFilledInOldRespondentAddressIsMigratedShouldReturnNewListStructureWithNullAddressFields() {
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
        Respondent expectedRespondent = createNewRespondent();

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
        assertThat(firstRespondentParty.get("telephoneNumber")).isEqualTo(objectMapper.convertValue(TelephoneNumber.builder().build(), Object.class));
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
    public void whenOldRespondentWithOneNameIsMigratedShouldReturnNewListStructureWithOnlyFirstName() {
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
    public void whenOldRespondentWithManyNamesIsMigratedShouldReturnNewListStructureWithMultipleFirstNames() {
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
            .party(Party.builder()
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
