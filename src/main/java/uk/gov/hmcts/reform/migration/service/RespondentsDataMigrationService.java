package uk.gov.hmcts.reform.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.domain.common.Address;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;
import uk.gov.hmcts.reform.domain.common.TelephoneNumber;
import uk.gov.hmcts.reform.fpl.domain.CaseData;
import uk.gov.hmcts.reform.fpl.domain.OldRespondent;
import uk.gov.hmcts.reform.fpl.domain.OldRespondents;
import uk.gov.hmcts.reform.fpl.domain.Respondent;
import uk.gov.hmcts.reform.fpl.domain.common.RespondentParty;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class RespondentsDataMigrationService implements DataMigrationService<CaseData> {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails != null && caseDetails.getData() != null &&
                !isEmpty(caseDetails.getData().get("respondents"));
    }

    @Override
    public CaseData migrate(Map<String, Object> data) {
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);

        CaseData migratedCaseData = CaseData.builder()
                .respondents1(migrateRespondents(caseData.getRespondents()))
                .build();

        log.info("new case details: {}", migratedCaseData);

        return migratedCaseData;
    }

    private List<CollectionEntry<Respondent>> migrateRespondents(OldRespondents respondents) {
        log.info("beginning to migrate respondents {}", respondents);

        List<CollectionEntry<Respondent>> migratedRespondents = respondents.getAll().stream()
                .map(entry -> migrateIndividualRespondent(entry.getValue()))
                .map(entry -> CollectionEntry.<Respondent>builder()
                        .id(UUID.randomUUID().toString())
                        .value(entry)
                        .build())
                .collect(toList());

        log.info("returning new structure {}", migratedRespondents);

        return migratedRespondents;
    }

    private Respondent migrateIndividualRespondent(OldRespondent or) {
        log.info("migrating respondent {}", or);

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

        TelephoneNumber telephoneNumber;

        if (isEmpty(or.getTelephone())) {
            telephoneNumber = null;
        } else {
            TelephoneNumber.TelephoneNumberBuilder telephoneNumberBuilder = TelephoneNumber.builder();
            telephoneNumberBuilder.telephoneNumber(defaultIfBlank(or.getTelephone(), null));
            telephoneNumber = telephoneNumberBuilder.build();
        }

        RespondentParty.RespondentPartyBuilder partyBuilder = RespondentParty.builder();
        partyBuilder.partyID(UUID.randomUUID().toString());

        if (!isEmpty(or.getName())) {
            partyBuilder.firstName(splitName(or.getName()).get(0));

            if (splitName(or.getName()).size() > 1) {
                partyBuilder.lastName(splitName(or.getName()).get(1));
            }
        }

        partyBuilder.dateOfBirth(defaultIfBlank(or.getDob(), null));
        partyBuilder.address(address);
        partyBuilder.telephoneNumber(telephoneNumber);
        partyBuilder.gender(defaultIfBlank(or.getGender(), null));
        partyBuilder.genderIdentification(defaultIfBlank(or.getGenderIdentify(), null));
        partyBuilder.placeOfBirth(defaultIfBlank(or.getPlaceOfBirth(), null));
        partyBuilder.relationshipToChild(defaultIfBlank(or.getRelationshipToChild(), null));
        partyBuilder.contactDetailsHidden(defaultIfBlank(or.getContactDetailsHidden(), null));
        partyBuilder.contactDetailsHiddenReason(defaultIfBlank(or.getContactDetailsHiddenReason(), null));
        partyBuilder.litigationIssues(defaultIfBlank(or.getLitigationIssues(), null));
        partyBuilder.litigationIssuesDetails(defaultIfBlank(or.getLitigationIssuesDetails(), null));
        RespondentParty party = partyBuilder.build();

        return Respondent.builder()
                .party(party)
                .build();
    }

    private List<String> splitName(String name) {
        ImmutableList.Builder<String> names = ImmutableList.builder();
        int index = name.lastIndexOf(" ");

        if (index == -1) {
            names.add(name);
        } else {
            names.add(name.substring(0, index));
            names.add(name.substring(index + 1));
        }

        return names.build();
    }
}
