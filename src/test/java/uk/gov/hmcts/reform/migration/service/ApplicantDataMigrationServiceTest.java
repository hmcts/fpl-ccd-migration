package uk.gov.hmcts.reform.migration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.Applicant;
import uk.gov.hmcts.reform.domain.OldApplicant;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
import uk.gov.hmcts.reform.domain.common.MobileNumber;
import uk.gov.hmcts.reform.domain.common.ApplicantParty;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicantDataMigrationServiceTest {
    private final String APPLICANT = "Applicant name";
    private final String EMAIL = "email@email.com";
    private final String MOBILE = "07778987656";
    private final String COUNTY = "County";
    private final String COUNTRY = "Country";
    private final String POSTCODE = "BT17NT";
    private final String POST_TOWN = "Ballymena";
    private final String ADDRESSLINE1 ="ADDRESSLINE1";
    private final String ADDRESSLINE2 = "ADDRESSLINE2";
    private final String ADDRESSLINE3 = "ADDRESSLINE3";
    private final String JOB_TITLE = "JobTitle";
    private final String TELEPHONE = "02825674837";
    private final String PERSON_TO_CONTACT = "Person to contact";
    private final String PARTY_TYPE = "ApplicantParty type";
    private final String LEAD_APPLICANT_INDICATOR = "Yes";
    private final String PARTY_ID = UUID.randomUUID().toString();
    private final String PBA_NUMBER = "PBA123456";

    private final ApplicantDataMigrationService service = new ApplicantDataMigrationService();

    @Test
    void whenOldStructureExistsAcceptsShouldReturnTrue() {
        Map<String, Object> data = new HashMap<>();

        OldApplicant applicant = oldApplicant();

        data.put("applicant", applicant);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        assertThat(service.accepts().test(caseDetails)).isEqualTo(true);
    }

    @Test
    void whenDataIsNullAcceptsShouldReturnFalse() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(null)
            .build();

        assertThat(service.accepts().test(caseDetails)).isEqualTo(false);
    }

    @Test
    void whenNewStructureDoesNotExistAcceptsShouldReturnFalse() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(ImmutableMap.of("data", "someData"))
            .build();

        assertThat(service.accepts().test(caseDetails)).isEqualTo(false);
    }

    @Test
    void whenOldApplicantStructureIsMigratedShouldReturnNewApplicantStructure() {
        Map<String, Object> data = new HashMap<>();

        OldApplicant applicant = oldApplicant();

        data.put("applicant", applicant);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);


        List<Map<String, Object>> valueInApplicant = (List<Map<String, Object>>) caseDetails.getData().get("applicants");
        Applicant newApplicant = newApplicant();

        assertThat(valueInApplicant.get(0).get("value").equals(newApplicant));
        assertThat(valueInApplicant.get(0).get("applicant")).isNull();
    }

    @Test
    void whenOldApplicantStructureIsEmptyAndMigratedShouldReturnNewEmptyApplicantStructure() {
        Map<String, Object> data = new HashMap<>();

        OldApplicant applicant = OldApplicant.builder()
            .name(null)
            .email(null)
            .mobile(null)
            .address(null)
            .jobTitle(null)
            .telephone(null)
            .personToContact(null)
            .pbaNumber(null)
            .build();

        data.put("applicant", applicant);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> valueInApplicant = (List<Map<String, Object>>) caseDetails.getData().get("applicants");

        assertThat(valueInApplicant.get(0).get("value").equals(valueInApplicant.get(0).get("value").equals(Applicant.builder()
            .party(ApplicantParty.builder()
                .partyId(null)
                .partyType(null)
                .organisationName(null)
                .address(null)
                .telephoneNumber(null)
                .mobileNumber(null)
                .jobTitle(null)
                .pbaNumber(null)
                .build())
            .leadApplicantIndicator(null)
            .build())));
        assertThat(valueInApplicant.get(0).get("applicant")).isNull();
    }

    @Test
    void whenPartiallyFilledInOldApplicantStructureIsMigratedShouldReturnNewListStructureWithNullFields() {
        Map<String, Object> data = new HashMap<>();
        OldApplicant applicant = OldApplicant.builder()
            .name(APPLICANT)
            .email(EMAIL)
            .build();
        
        data.put("applicant", applicant);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> valueInApplicant = (List<Map<String, Object>>) caseDetails.getData().get("applicants");

        assertThat(valueInApplicant).hasSize(1);
        assertThat(valueInApplicant.get(0).get("value").equals(Applicant.builder()
            .party(ApplicantParty.builder()
                .partyId(null)
                .partyType(null)
                .organisationName(APPLICANT)
                .address(Address.builder()
                    .build())
                .email(EmailAddress.builder()
                    .email(EMAIL)
                    .build())
                .telephoneNumber(TelephoneNumber.builder()
                    .build())
                .mobileNumber(MobileNumber.builder()
                    .build())
                .jobTitle(null)
                .pbaNumber(null)
                .build())
                .leadApplicantIndicator(null)
            .build()));
        assertThat(valueInApplicant.get(0).get("applicant")).isNull();
    }

    @Test
    void whenPartiallyFilledInOldApplicantAddressIsMigratedShouldReturnNewListStructureWithNullFields() {
        Map<String, Object> data = new HashMap<>();
        OldApplicant applicant = OldApplicant.builder()
            .address(Address.builder()
                .postcode(POSTCODE)
                .county(COUNTY)
                .build())
            .build();

        data.put("applicant", applicant);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);

        List<Map<String, Object>> valueInApplicant = (List<Map<String, Object>>) caseDetails.getData().get("applicants");

        assertThat(valueInApplicant).hasSize(1);
        assertThat(valueInApplicant.get(0).get("value").equals(Applicant.builder()
            .party(ApplicantParty.builder()
                .partyId(null)
                .partyType(null)
                .organisationName(null)
                .address(Address.builder()
                    .addressLine1(null)
                    .addressLine2(null)
                    .addressLine3(null)
                    .postcode(POSTCODE)
                    .country(COUNTRY)
                    .county(null)
                    .build())
                .email(EmailAddress.builder()
                    .email(null)
                    .build())
                .telephoneNumber(TelephoneNumber.builder()
                    .telephoneNumber(null)
                    .build())
                .mobileNumber(MobileNumber.builder()
                    .telephoneNumber(null)
                    .build())
                .jobTitle(null)
                .pbaNumber(null)
                .build())
            .leadApplicantIndicator(null)
            .build()));
        assertThat(valueInApplicant.get(0).get("applicant")).isNull();
    }

    private Applicant newApplicant() {
        Applicant newApplicant = Applicant.builder()
            .party(ApplicantParty.builder()
                .partyId(PARTY_ID)
                .partyType(PARTY_TYPE)
                .organisationName(APPLICANT)
                .address(Address.builder()
                    .addressLine1(ADDRESSLINE1)
                    .addressLine2(ADDRESSLINE2)
                    .addressLine3(ADDRESSLINE3)
                    .postTown(POST_TOWN)
                    .postcode(POSTCODE)
                    .county(COUNTY)
                    .country(COUNTRY)
                    .build())
                .email(EmailAddress.builder()
                    .email(EMAIL)
                    .build())
                .telephoneNumber(TelephoneNumber.builder()
                    .telephoneNumber(TELEPHONE)
                    .contactDirection(PERSON_TO_CONTACT)
                    .build())
                .mobileNumber(MobileNumber.builder()
                    .telephoneNumber(MOBILE)
                    .build())
                .jobTitle(JOB_TITLE)
                .pbaNumber(PBA_NUMBER)
                .build())
            .leadApplicantIndicator(LEAD_APPLICANT_INDICATOR)
            .build();
        return newApplicant;
    }

    private OldApplicant oldApplicant() {
        return OldApplicant.builder()
                .name(APPLICANT)
                .email(EMAIL)
                .mobile(MOBILE)
                .address(Address.builder()
                    .county(COUNTY)
                    .country(COUNTRY)
                    .postcode(POSTCODE)
                    .postTown(POST_TOWN)
                    .addressLine1(ADDRESSLINE1)
                    .addressLine2(ADDRESSLINE2)
                    .addressLine3(ADDRESSLINE3)
                    .build())
                .jobTitle(JOB_TITLE)
                .telephone(TELEPHONE)
                .personToContact(PERSON_TO_CONTACT)
                .pbaNumber(PBA_NUMBER)
                .build();
    }
}
