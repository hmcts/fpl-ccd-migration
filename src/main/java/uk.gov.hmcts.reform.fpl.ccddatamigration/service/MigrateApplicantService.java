package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.Applicant;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldApplicant;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Email;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.MobileNumber;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Party;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.TelephoneNumber;

import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Slf4j
@Service
public class MigrateApplicantService {

    private ObjectMapper objectMapper = new ObjectMapper();

    CaseDetails migrateCase(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        Map<String, Object> map = ImmutableMap.of(
            "id", UUID.randomUUID().toString(),
            "value", migrateApplicant(objectMapper.convertValue(data.get("applicant"), OldApplicant.class)));

        data.put("applicants", ImmutableList.of(map));
        data.put("applicant", null);

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(data)
            .build();

        log.info("New case details: {}", caseDetails1);

        return caseDetails1;
    }

    private Applicant migrateApplicant(OldApplicant or) {
        log.info("Migrating applicant: {}", or);

        Address.AddressBuilder addressBuilder = Address.builder();
        addressBuilder.addressLine1(defaultIfBlank(or.getAddress().getAddressLine1(), null));
        addressBuilder.addressLine2(defaultIfBlank(or.getAddress().getAddressLine2(), null));
        addressBuilder.addressLine3(defaultIfBlank(or.getAddress().getAddressLine3(), null));
        addressBuilder.postTown(defaultIfBlank(or.getAddress().getPostTown(), null));
        addressBuilder.postcode(defaultIfBlank(or.getAddress().getPostcode(), null));
        addressBuilder.county(defaultIfBlank(or.getAddress().getCounty(), null));
        addressBuilder.country(defaultIfBlank(or.getAddress().getCountry(), null));
        Address address = addressBuilder.build();

        Email.EmailBuilder emailBuilder = Email.builder();
        emailBuilder.email(defaultIfBlank(or.getEmail(), null));
        Email email = emailBuilder.build();

        TelephoneNumber.TelephoneNumberBuilder telephoneNumberBuilder = TelephoneNumber.builder();
        telephoneNumberBuilder.telephoneNumber(defaultIfBlank(or.getTelephone(), null));
        telephoneNumberBuilder.contactDirection(defaultIfBlank(or.getPersonToContact(), null));
        TelephoneNumber telephoneNumber = telephoneNumberBuilder.build();

        MobileNumber.MobileNumberBuilder mobileNumberBuilder = MobileNumber.builder();
        mobileNumberBuilder.telephoneNumber(defaultIfBlank(or.getMobile(), null));
        MobileNumber mobileNumber = mobileNumberBuilder.build();

        Party.PartyBuilder partyBuilder = Party.builder();
        partyBuilder.partyId(UUID.randomUUID().toString());
        partyBuilder.partyType("Individual");
        partyBuilder.name(defaultIfBlank(or.getName().split("\\s+")[0], null));
        partyBuilder.address(address);
        partyBuilder.emailAddress(email);
        partyBuilder.telephoneNumber(telephoneNumber);
        partyBuilder.mobileNumber(mobileNumber);
        partyBuilder.jobTitle(defaultIfBlank(or.getJobTitle(), null));
        partyBuilder.pbaNumber(defaultIfBlank(or.getPbaNumber(), null));
        Party party = partyBuilder.build();

        return Applicant.builder()
            .party(party)
            .build();
    }
}
