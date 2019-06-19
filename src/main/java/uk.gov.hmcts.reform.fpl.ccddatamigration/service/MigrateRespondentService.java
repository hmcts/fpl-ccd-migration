package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldRespondent;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.Respondent;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Party;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.TelephoneNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class MigrateRespondentService {

    private ObjectMapper objectMapper = new ObjectMapper();

    CaseDetails migrateCase(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        // ADD NEW STRUCTURE TO CASE DATA

        data.put("respondents1", migrateRespondents(objectMapper.convertValue(data.get("respondents"), Map.class)));
        data.put("respondents", null);

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(data)
            .build();

        log.info("new case details: {}", caseDetails1);

        return caseDetails1;
    }

    private List<Map<String, Object>> migrateRespondents(Map<String, Object> respondents) {
        log.info("beginning to migrate respondents {}", respondents);

        /// FIRST RESPONDENT

        OldRespondent firstRespondent =
            objectMapper.convertValue(respondents.get("firstRespondent"), OldRespondent.class);
        Respondent migratedFirstRespondent = migrateIndividualRespondent(firstRespondent);

        /// ADDITIONAL RESPONDENT

        List<Map<String, Object>> additionalRespondents =
            (List<Map<String, Object>>) objectMapper.convertValue(respondents.get("additional"), List.class);

        List<Respondent> migratedRespondentCollection = additionalRespondents.stream()
            .map(respondent ->
                migrateIndividualRespondent(objectMapper.convertValue(respondent.get("value"), OldRespondent.class)))
            .collect(toList());

        // ADD FIRST RESPONDENT TO ADDITIONAL RESPONDENT LIST

        migratedRespondentCollection.add(migratedFirstRespondent);

        List<Map<String, Object>> newStructure = new ArrayList<>();

        /// BUILD NEW STRUCTURE

        migratedRespondentCollection.forEach(item -> {
            Map map = objectMapper.convertValue(item, Map.class);
            System.out.println("map = " + map);

            newStructure.add(ImmutableMap.of(
                "id", UUID.randomUUID().toString(),
                "value", map));
        });

        log.info("returning new structure {}", newStructure);

        return newStructure;
    }

    private Respondent migrateIndividualRespondent(OldRespondent or) {
        log.info("migrating respondent {}", or);

        Address.AddressBuilder addressBuilder = Address.builder();
        addressBuilder.addressLine1(defaultIfBlank(or.getAddress().getAddressLine1(), null));
        addressBuilder.addressLine2(defaultIfBlank(or.getAddress().getAddressLine2(), null));
        addressBuilder.addressLine3(defaultIfBlank(or.getAddress().getAddressLine3(), null));
        addressBuilder.postTown(defaultIfBlank(or.getAddress().getPostTown(), null));
        addressBuilder.postcode(defaultIfBlank(or.getAddress().getPostcode(), null));
        addressBuilder.county(defaultIfBlank(or.getAddress().getCounty(), null));
        addressBuilder.country(defaultIfBlank(or.getAddress().getCountry(), null));
        Address address = addressBuilder.build();

        TelephoneNumber.TelephoneNumberBuilder telephoneNumberBuilder = TelephoneNumber.builder();
        telephoneNumberBuilder.telephoneNumber(defaultIfBlank(or.getTelephone(), null));
        TelephoneNumber telephoneNumber = telephoneNumberBuilder.build();

        Party.PartyBuilder partyBuilder = Party.builder();
        partyBuilder.partyID(UUID.randomUUID().toString());
        partyBuilder.partyType("Individual");
        partyBuilder.firstName(defaultIfBlank(or.getName().split("\\s+")[0], null));
        partyBuilder.lastName(defaultIfBlank(or.getName().split("\\s+")[1], null));
        partyBuilder.dateOfBirth(defaultIfBlank(or.getDob(), null));
        partyBuilder.address(address);
        partyBuilder.telephoneNumber(telephoneNumber);
        partyBuilder.gender(defaultIfBlank(or.getGender(), null));
        partyBuilder.genderIdentification(defaultIfBlank(or.getGenderIdentify(), null));
        partyBuilder.placeOfBirth(defaultIfBlank(or.getPlaceOfBirth(), null));
        partyBuilder.relationshipToChild(defaultIfBlank(or.getRelationshipToChild(), null));
        partyBuilder.contactDetailsHidden(defaultIfBlank(or.getContactDetailsHidden(), null));
        partyBuilder.litigationIssues(defaultIfBlank(or.getLitigationIssues(), null));
        partyBuilder.litigationIssuesDetails(defaultIfBlank(or.getLitigationIssuesDetails(), null));
        Party party = partyBuilder.build();

        return Respondent.builder()
            .party(party)
            .build();
    }
}
