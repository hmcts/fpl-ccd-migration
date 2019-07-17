package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.Applicant;
import uk.gov.hmcts.reform.domain.CaseData;
import uk.gov.hmcts.reform.domain.OldApplicant;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
import uk.gov.hmcts.reform.domain.common.MobileNumber;
import uk.gov.hmcts.reform.domain.common.ApplicantParty;
import uk.gov.hmcts.reform.domain.common.PartyType;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class ApplicantDataMigrationService implements DataMigrationService<CaseData> {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails != null && caseDetails.getData() != null &&
            !isEmpty(caseDetails.getData().get("applicant"));
    }

    @Override
    public CaseData migrate(Map<String, Object> data) {
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);

        CaseData migratedCaseData = CaseData.builder()
            .applicants(ImmutableList.of(CollectionEntry.<Applicant>builder()
                .id(UUID.randomUUID().toString())
                .value(migrateApplicant(caseData.getApplicant()))
                .build()))
            .build();

        log.info("New case details: {}", migratedCaseData);

        return migratedCaseData;
    }

    private Applicant migrateApplicant(OldApplicant or) {
        log.info("Migrating applicant: {}", or);

        Address.AddressBuilder addressBuilder = Address.builder();
        if (!isEmpty(or.getAddress())) {
            addressBuilder.addressLine1(defaultIfBlank(or.getAddress().getAddressLine1(), null));
            addressBuilder.addressLine2(defaultIfBlank(or.getAddress().getAddressLine2(), null));
            addressBuilder.addressLine3(defaultIfBlank(or.getAddress().getAddressLine3(), null));
            addressBuilder.postTown(defaultIfBlank(or.getAddress().getPostTown(), null));
            addressBuilder.postcode(defaultIfBlank(or.getAddress().getPostcode(), null));
            addressBuilder.county(defaultIfBlank(or.getAddress().getCounty(), null));
            addressBuilder.country(defaultIfBlank(or.getAddress().getCountry(), null));
        }
        Address address = addressBuilder.build();

        EmailAddress.EmailAddressBuilder emailBuilder = EmailAddress.builder();
        emailBuilder.email(defaultIfBlank(or.getEmail(), null));
        EmailAddress email = emailBuilder.build();

        TelephoneNumber.TelephoneNumberBuilder telephoneNumberBuilder = TelephoneNumber.builder();
        telephoneNumberBuilder.telephoneNumber(defaultIfBlank(or.getTelephone(), null));
        telephoneNumberBuilder.contactDirection(defaultIfBlank(or.getPersonToContact(), null));
        TelephoneNumber telephoneNumber = telephoneNumberBuilder.build();

        MobileNumber.MobileNumberBuilder mobileNumberBuilder = MobileNumber.builder();
        mobileNumberBuilder.telephoneNumber(defaultIfBlank(or.getMobile(), null));
        MobileNumber mobileNumber = mobileNumberBuilder.build();

        ApplicantParty.ApplicantPartyBuilder partyBuilder = ApplicantParty.builder();
        partyBuilder.partyId(UUID.randomUUID().toString());
        partyBuilder.partyType(PartyType.INDIVIDUAL.name());
        if (!isEmpty(or.getName())) {
            partyBuilder.organisationName(defaultIfBlank(or.getName(), null));
        }
        partyBuilder.address(address);
        partyBuilder.email(email);
        partyBuilder.telephoneNumber(telephoneNumber);
        partyBuilder.mobileNumber(mobileNumber);
        partyBuilder.jobTitle(defaultIfBlank(or.getJobTitle(), null));
        partyBuilder.pbaNumber(defaultIfBlank(or.getPbaNumber(), null));
        ApplicantParty party = partyBuilder.build();

        return Applicant.builder()
            .party(party)
            .build();
    }
}
