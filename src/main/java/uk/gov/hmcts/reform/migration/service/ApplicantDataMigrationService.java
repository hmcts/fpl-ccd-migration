package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.Applicant;
import uk.gov.hmcts.reform.domain.CaseData;
import uk.gov.hmcts.reform.domain.OldApplicant;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.ApplicantParty;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;
import uk.gov.hmcts.reform.domain.common.EmailAddress;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
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
            .applicants(migrateApplicant(caseData.getApplicant()))
            .build();

        log.info("New case details: {}", migratedCaseData);

        return migratedCaseData;
    }

    private List<CollectionEntry<Applicant>> migrateApplicant(OldApplicant oa) {
        log.info("Migrating applicant: {}", oa);

        Address.AddressBuilder addressBuilder = Address.builder();

        if (!isEmpty(oa.getAddress())) {
            addressBuilder.addressLine1(defaultIfBlank(oa.getAddress().getAddressLine1(), null));
            addressBuilder.addressLine2(defaultIfBlank(oa.getAddress().getAddressLine2(), null));
            addressBuilder.addressLine3(defaultIfBlank(oa.getAddress().getAddressLine3(), null));
            addressBuilder.postTown(defaultIfBlank(oa.getAddress().getPostTown(), null));
            addressBuilder.postcode(defaultIfBlank(oa.getAddress().getPostcode(), null));
            addressBuilder.county(defaultIfBlank(oa.getAddress().getCounty(), null));
            addressBuilder.country(defaultIfBlank(oa.getAddress().getCountry(), null));
        }
        Address address = addressBuilder.build();

        EmailAddress email;

        if (isEmpty(oa.getEmail())) {
            email = null;
        } else {
            EmailAddress.EmailAddressBuilder emailBuilder = EmailAddress.builder();
            emailBuilder.email(defaultIfBlank(oa.getEmail(), null));
            email = emailBuilder.build();
        }

        TelephoneNumber telephoneNumber;

        if (isEmpty(oa.getTelephone())) {
            telephoneNumber = null;
        } else {
            TelephoneNumber.TelephoneNumberBuilder telephoneNumberBuilder = TelephoneNumber.builder();
            telephoneNumberBuilder.telephoneNumber(defaultIfBlank(oa.getTelephone(), null));
            telephoneNumberBuilder.contactDirection(defaultIfBlank(oa.getPersonToContact(), null));
            telephoneNumber = telephoneNumberBuilder.build();
        }

        TelephoneNumber mobileNumber;

        if (isEmpty(oa.getMobile())) {
            mobileNumber = null;
        } else {
            TelephoneNumber.TelephoneNumberBuilder mobileNumberBuilder = TelephoneNumber.builder();
            mobileNumberBuilder.telephoneNumber(defaultIfBlank(oa.getMobile(), null));
            mobileNumber = mobileNumberBuilder.build();
        }

        ApplicantParty.ApplicantPartyBuilder partyBuilder = ApplicantParty.builder();
        partyBuilder.partyId(UUID.randomUUID().toString());
        partyBuilder.partyType("ORGANISATION");
        partyBuilder.organisationName(defaultIfBlank(oa.getName(), null));
        partyBuilder.address(address);
        partyBuilder.email(email);
        partyBuilder.telephoneNumber(telephoneNumber);
        partyBuilder.mobileNumber(mobileNumber);
        partyBuilder.jobTitle(defaultIfBlank(oa.getJobTitle(), null));
        partyBuilder.pbaNumber(defaultIfBlank(oa.getPbaNumber(), null));
        ApplicantParty party = partyBuilder.build();

        CollectionEntry<Applicant> applicantEntry = CollectionEntry.<Applicant>builder()
            .id(UUID.randomUUID().toString())
            .value(Applicant.builder()
                .party(party)
                .leadApplicantIndicator("Yes")
                .build())
            .build();

        List<CollectionEntry<Applicant>> applicants = new ArrayList<>();
        applicants.add(applicantEntry);

        return applicants;
    }
}
