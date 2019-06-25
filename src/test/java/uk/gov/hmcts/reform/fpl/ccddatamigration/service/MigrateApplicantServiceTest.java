package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.Applicant;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldApplicant;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Email;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.MobileNumber;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Party;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.TelephoneNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class MigrateApplicantServiceTest {
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
    private final String PARTYTYPE = "Party type";
    private final String LEADAPPLICANTINDICATOR = "Yes";
    private final String PARTYID = UUID.randomUUID().toString();
    private final String PBANUMBER= "PBA123456";

    @InjectMocks
    private final MigrateApplicantService service = new MigrateApplicantService();

    @Test
    public void mapOldApplicantToNewApplicant() {
        Map<String, Object> data = new HashMap<>();

        OldApplicant applicant = getOldApplicant();

        Applicant newApplicant = newApplicantBuilder();

        data.put("applicant", applicant);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        CaseDetails caseDetails1 = service.migrateCase(caseDetails);

        Map<String, Object> valueInApplicant = (Map<String, Object>) caseDetails1.getData().get("applicants");

        assertThat(valueInApplicant.get("value").equals(newApplicant));
    }

    private Applicant newApplicantBuilder() {
        Applicant newApplicant = Applicant.builder()
            .party(Party.builder()
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
                .emailAddress(Email.builder()
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
