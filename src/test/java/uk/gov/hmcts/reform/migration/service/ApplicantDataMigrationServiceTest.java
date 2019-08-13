package uk.gov.hmcts.reform.migration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.Applicant;
import uk.gov.hmcts.reform.domain.CaseData;
import uk.gov.hmcts.reform.domain.OldApplicant;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.ApplicantParty;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicantDataMigrationServiceTest {
    private final String APPLICANT = "Applicant name";
    private final String EMAIL = "email@email.com";
    private final String MOBILE = "07778987656";
    private final String COUNTY = "County";
    private final String COUNTRY = "Country";
    private final String POSTCODE = "BT17NT";
    private final String POST_TOWN = "Ballymena";
    private final String ADDRESSLINE1 = "ADDRESSLINE1";
    private final String ADDRESSLINE2 = "ADDRESSLINE2";
    private final String ADDRESSLINE3 = "ADDRESSLINE3";
    private final String JOB_TITLE = "JobTitle";
    private final String TELEPHONE = "02825674837";
    private final String PERSON_TO_CONTACT = "Person to contact";
    private final String PBA_NUMBER = "PBA123456";

    @Autowired
    private ApplicantDataMigrationService service;

    @Test
    void whenOldStructureExistsAcceptsShouldReturnTrue() {
        Map<String, Object> data = new HashMap<>();

        OldApplicant applicant = createOldApplicant();

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

        data.put("applicant", createOldApplicant());

        CaseData migratedData = service.migrate(data);

        ApplicantParty actualValue = migratedData.getApplicants().get(0).getValue().getParty();
        ApplicantParty expectedValue = createNewApplicant().getParty();

        assertThat(migratedData.getApplicant()).isNull();

        assertThat(actualValue.getPartyId()).isNotNull();
        assertThat(actualValue.getPartyType()).isNull();
        assertThat(actualValue.getOrganisationName()).isEqualTo(expectedValue.getOrganisationName());
        assertThat(actualValue.getPbaNumber()).isEqualTo(expectedValue.getPbaNumber());
        assertThat(actualValue.getAddress()).isEqualTo(expectedValue.getAddress());
        assertThat(actualValue.getTelephoneNumber()).isEqualTo(expectedValue.getTelephoneNumber());
        assertThat(actualValue.getJobTitle()).isEqualTo(expectedValue.getJobTitle());
        assertThat(actualValue.getMobileNumber()).isEqualTo(expectedValue.getMobileNumber());
        assertThat(actualValue.getEmailAddress()).isEqualTo(expectedValue.getEmailAddress());
    }

    @Test
    void whenOldApplicantContainsNullValuesShouldReturnNewApplicantWithPartyIdAndNullValues() {
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

        CaseData migratedData = service.migrate(data);

        ApplicantParty actualValue = migratedData.getApplicants().get(0).getValue().getParty();

        assertThat(migratedData.getApplicant()).isNull();
        assertThat(actualValue.getPartyId()).isNotNull();
        assertThat(actualValue.getPartyType()).isNull();
        assertThat(actualValue.getOrganisationName()).isNull();
        assertThat(actualValue.getPbaNumber()).isNull();
        assertThat(actualValue.getAddress()).isEqualTo(Address.builder().build());
        assertThat(actualValue.getTelephoneNumber()).isNull();
        assertThat(actualValue.getJobTitle()).isNull();
        assertThat(actualValue.getMobileNumber()).isNull();
        assertThat(actualValue.getEmailAddress()).isNull();
    }

    @Test
    void whenPartiallyFilledInOldApplicantStructureIsMigratedShouldReturnNewListStructureWithNullFields() {
        Map<String, Object> data = new HashMap<>();
        OldApplicant applicant = OldApplicant.builder()
            .name(APPLICANT)
            .email(EMAIL)
            .build();

        data.put("applicant", applicant);

        CaseData migratedData = service.migrate(data);

        ApplicantParty actualValue = migratedData.getApplicants().get(0).getValue().getParty();

        assertThat(migratedData.getApplicant()).isNull();
        assertThat(actualValue.getPartyId()).isNotNull();
        assertThat(actualValue.getPartyType()).isNull();
        assertThat(actualValue.getOrganisationName()).isEqualTo(APPLICANT);
        assertThat(actualValue.getPbaNumber()).isNull();
        assertThat(actualValue.getAddress()).isEqualTo(Address.builder().build());
        assertThat(actualValue.getTelephoneNumber()).isNull();
        assertThat(actualValue.getJobTitle()).isNull();
        assertThat(actualValue.getMobileNumber()).isNull();
        assertThat(actualValue.getEmailAddress()).isEqualTo(EmailAddress.builder().email(EMAIL).build());
    }

    private Applicant createNewApplicant() {
        return Applicant.builder()
            .party(ApplicantParty.builder()
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
                .emailAddress(EmailAddress.builder()
                    .email(EMAIL)
                    .build())
                .telephoneNumber(TelephoneNumber.builder()
                    .telephoneNumber(TELEPHONE)
                    .contactDirection(PERSON_TO_CONTACT)
                    .build())
                .mobileNumber(TelephoneNumber.builder()
                    .telephoneNumber(MOBILE)
                    .build())
                .jobTitle(JOB_TITLE)
                .pbaNumber(PBA_NUMBER)
                .build())
            .leadApplicantIndicator("Yes")
            .build();
    }

    private OldApplicant createOldApplicant() {
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
