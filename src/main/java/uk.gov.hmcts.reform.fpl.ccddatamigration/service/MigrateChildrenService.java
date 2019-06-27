package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.Child;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldChild;
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
public class MigrateChildrenService {
    private ObjectMapper objectMapper = new ObjectMapper();

    CaseDetails migrateCase(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        data.put("children1", migrateChildren(objectMapper.convertValue(data.get("children"), Map.class)));
        data.put("children", null);

        CaseDetails caseDetails1 = CaseDetails.builder()
                .data(data)
                .build();

        log.info("new case details: {}", caseDetails1);

        return caseDetails;
    }

    private List<Map<String, Object>> migrateChildren(Map<String, Object> children) {
        log.info("beginning to migrate children {}", children);

        /// FIRST CHILD
        OldChild firstChild =
                objectMapper.convertValue(children.get("firstChild"), OldChild.class);
        Child migratedFirstChild = migrateIndividualChild(firstChild);

        /// ADDITIONAL RESPONDENT
        List<Map<String, Object>> additionalChildren =
                (List<Map<String, Object>>) objectMapper.convertValue(children.get("additionalChildren"), List.class);

        List<Child> migratedChildrenCollection = additionalChildren.stream()
                .map(child ->
                        migrateIndividualChild(objectMapper.convertValue(child.get("value"), OldChild.class)))
                .collect(toList());

        // ADD FIRST RESPONDENT TO ADDITIONAL RESPONDENT LIST
        migratedChildrenCollection.add(migratedFirstChild);
        List<Map<String, Object>> newStructure = new ArrayList<>();

        /// BUILD NEW STRUCTURE
        migratedChildrenCollection.forEach(item -> {
            Map map = objectMapper.convertValue(item, Map.class);
            System.out.println("map = " + map);

            newStructure.add(ImmutableMap.of(
                    "id", UUID.randomUUID().toString(),
                    "value", map));
        });

        log.info("returning new structure {}", newStructure);

        return newStructure;
    }

    private Child migrateIndividualChild(OldChild oc) {
        log.info("migrating respondent {}", oc);

        Address.AddressBuilder addressBuilder = Address.builder();
        addressBuilder.addressLine1(defaultIfBlank(oc.getAddress().getAddressLine1(), null));
        addressBuilder.addressLine2(defaultIfBlank(oc.getAddress().getAddressLine2(), null));
        addressBuilder.addressLine3(defaultIfBlank(oc.getAddress().getAddressLine3(), null));
        addressBuilder.postTown(defaultIfBlank(oc.getAddress().getPostTown(), null));
        addressBuilder.postcode(defaultIfBlank(oc.getAddress().getPostcode(), null));
        addressBuilder.county(defaultIfBlank(oc.getAddress().getCounty(), null));
        addressBuilder.country(defaultIfBlank(oc.getAddress().getCountry(), null));
        Address address = addressBuilder.build();

        TelephoneNumber telephoneNumber = TelephoneNumber.builder().build();

        Party.PartyBuilder partyBuilder = Party.builder();
        partyBuilder.partyID(UUID.randomUUID().toString());
        partyBuilder.partyType("Individual");
        partyBuilder.firstName(defaultIfBlank(oc.getChildName().split("\\s+")[0], null));
        partyBuilder.lastName(defaultIfBlank(oc.getChildName().split("\\s+")[1], null));
        partyBuilder.dateOfBirth(defaultIfBlank(oc.getChildDOB().toString(), null));
        partyBuilder.address(address);
        partyBuilder.telephoneNumber(telephoneNumber);
        partyBuilder.gender(defaultIfBlank(oc.getChildGender(), null));
        partyBuilder.genderIdentification(defaultIfBlank(oc.getChildGenderIdentification(), null));
        partyBuilder.livingSituation(defaultIfBlank(oc.getLivingSituation(), null));
        partyBuilder.situationDetails(defaultIfBlank(oc.getSituationDetails(), null));
        partyBuilder.situationDate(defaultIfBlank(oc.getSituationDate(), null ));
        partyBuilder.keyDates(defaultIfBlank(oc.getKeyDates(), null));
        partyBuilder.careAndContact(defaultIfBlank(oc.getCareAndContact(), null));
        partyBuilder.adoption(defaultIfBlank(oc.getAdoption(), null));
        partyBuilder.placementOrderApplication(defaultIfBlank(oc.getPlacementOrderApplication(), null));
        partyBuilder.placementCourt(defaultIfBlank(oc.getPlacementCourt(), null));
        partyBuilder.mothersName(defaultIfBlank(oc.getMothersName(), null));
        partyBuilder.fathersName(defaultIfBlank(oc.getFathersName(), null));
        partyBuilder.fathersResponsibility(defaultIfBlank(oc.getFathersResponsibility(), null));
        partyBuilder.socialWorkerName(defaultIfBlank(oc.getSocialWorkerName(), null));
        partyBuilder.socialWorkerTel(defaultIfBlank(oc.getSocialWorkerTel(), null));
        partyBuilder.additionalNeeds(defaultIfBlank(oc.getAdditionalNeeds(), null));
        partyBuilder.additionalNeedsDetails(defaultIfBlank(oc.getAdditionalNeedsDetails(), null));
        partyBuilder.detailsHidden(defaultIfBlank(oc.getDetailsHidden(), null));
        partyBuilder.detailsHiddenReason(defaultIfBlank(oc.getDetailsHiddenReason(), null));
        partyBuilder.litigationIssues(defaultIfBlank(oc.getLitigationIssues(), null));
        partyBuilder.litigationIssuesDetails(defaultIfBlank(oc.getLitigationIssuesDetails(), null));
        Party party = partyBuilder.build();

        return Child.builder()
                .party(party)
                .build();

    }
}
