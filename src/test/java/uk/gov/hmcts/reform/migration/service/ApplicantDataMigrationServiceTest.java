package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.Applicant;
import uk.gov.hmcts.reform.domain.OldApplicant;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
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
    private final String POSTTOWN = "Ballymena";
    private final String ADDRESSLINE1 ="ADDRESSLINE1";
    private final String ADDRESSLINE2 = "ADDRESSLINE2";
    private final String ADDRESSLINE3 = "ADDRESSLINE3";
    private final String JOBTITLE = "JobTitle";
    private final String TELEPHONE = "02825674837";
    private final String PERSONTOCONTACT = "Person to contact";
    private final String PARTYTYPE = "ApplicantParty type";
    private final String LEADAPPLICANTINDICATOR = "Yes";
    private final String PARTYID = UUID.randomUUID().toString();
    private final String PBANUMBER= "PBA123456";

    private final ApplicantDataMigrationService service = new ApplicantDataMigrationService();
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

    @Test
    void mapOldApplicantToNewApplicant() {
        Map<String, Object> data = new HashMap<>();

        OldApplicant applicant = getOldApplicant();

        Applicant newApplicant = newApplicantBuilder();

        data.put("applicant", applicant);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        service.migrate(caseDetails);


        List<Map<String, Object>> valueInApplicant = (List<Map<String, Object>>) caseDetails.getData().get("applicants");

        assertThat(valueInApplicant.get(0).get("value").equals(newApplicant));
    }


//    private Map<String, Object> createPartialOldApplicant() {
//        Map<String, Object> data = new HashMap<>();
//        OldApplicant applicant;
//
//        applicant = OldApplicant.builder()
//            .name(APPLICANT)
//            .build();
//
//        data.put("applicant", OldApplicant.builder().build());
//        return data;
//    }

    private Map<String, Object> createOldApplicant() {
        Map<String, Object> data = new HashMap<>();
        OldApplicant applicant;

            applicant = OldApplicant.builder()
                .name(APPLICANT)
                .email(EMAIL)
                .mobile(MOBILE)
                .address(Address.builder()
                    .addressLine1(ADDRESSLINE1)
                    .addressLine2(ADDRESSLINE2)
                    .addressLine3(ADDRESSLINE3)
                    .country(COUNTRY)
                    .county(COUNTY)
                    .postcode(POSTCODE)
                    .postTown(POSTTOWN)
                    .build())
                .jobTitle(JOBTITLE)
                .telephone(TELEPHONE)
                .personToContact(PERSONTOCONTACT)
                .pbaNumber(PBANUMBER)
                .build();

        data.put("applicant", OldApplicant.builder().build());
        return data;
    }

    private Applicant newApplicantBuilder() {
        Applicant newApplicant = Applicant.builder()
            .party(ApplicantParty.builder()
                .partyId(PARTYID)
                .partyType(PARTYTYPE)
                .name(APPLICANT)
                .address(Address.builder()
                    .addressLine1(ADDRESSLINE1)
                    .addressLine2(ADDRESSLINE2)
                    .addressLine3(ADDRESSLINE3)
                    .postTown(POSTTOWN)
                    .postcode(POSTCODE)
                    .county(COUNTY)
                    .country(COUNTRY)
                    .build())
                .emailAddress(EmailAddress.builder()
                    .email(EMAIL)
                    .build())
                .telephoneNumber(TelephoneNumber.builder()
                    .telephoneNumber(TELEPHONE)
                    .contactDirection(PERSONTOCONTACT)
                    .build())
                .mobileNumber(MobileNumber.builder()
                    .telephoneNumber(MOBILE)
                    .build())
                .jobTitle(JOBTITLE)
                .pbaNumber(PBANUMBER)
                .build())
            .leadApplicantIndicator(LEADAPPLICANTINDICATOR)
            .build();
        return newApplicant;
    }

    private OldApplicant getOldApplicant() {
        return OldApplicant.builder()
                .name(APPLICANT)
                .email(EMAIL)
                .mobile(MOBILE)
                .address(Address.builder()
                    .county(COUNTY)
                    .country(COUNTRY)
                    .postcode(POSTCODE)
                    .postTown(POSTTOWN)
                    .addressLine1(ADDRESSLINE1)
                    .addressLine2(ADDRESSLINE2)
                    .addressLine3(ADDRESSLINE3)
                    .build())
                .jobTitle(JOBTITLE)
                .telephone(TELEPHONE)
                .personToContact(PERSONTOCONTACT)
                .pbaNumber(PBANUMBER)
                .build();
    }
}
