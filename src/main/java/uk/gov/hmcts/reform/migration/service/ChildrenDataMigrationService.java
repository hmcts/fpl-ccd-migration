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
import uk.gov.hmcts.reform.fpl.domain.OldChildren;
import uk.gov.hmcts.reform.fpl.domain.OldChild;
import uk.gov.hmcts.reform.fpl.domain.Child;
import uk.gov.hmcts.reform.fpl.domain.common.ChildParty;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class ChildrenDataMigrationService implements DataMigrationService<CaseData> {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails != null && caseDetails.getData() != null &&
            !isEmpty(caseDetails.getData().get("children"));
    }

    @Override
    public CaseData migrate(Map<String, Object> data) {
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);

        CaseData migratedCaseData = CaseData.builder()
            .children1(migrateChildren(caseData.getChildren()))
            .build();

        log.info("new case details: {}", migratedCaseData);

        return migratedCaseData;
    }

    private List<CollectionEntry<Child>> migrateChildren(OldChildren children) {
        log.info("beginning to migrate children {}", children);

        List<CollectionEntry<Child>> migratedChildren = children.getAll().stream()
            .map(entry -> migrateIndividualChild(entry.getValue()))
            .map(entry -> CollectionEntry.<Child>builder()
                .id(UUID.randomUUID().toString())
                .value(entry)
                .build())
            .collect(toList());

        log.info("returning new structure {}", migratedChildren);

        return migratedChildren;
    }

    private Child migrateIndividualChild(OldChild oc) {
        log.info("migrating child {}", oc);

        Address.AddressBuilder addressBuilder = Address.builder();
        if (!isEmpty(oc.getAddress())) {
            addressBuilder.addressLine1(defaultIfBlank(oc.getAddress().getAddressLine1(), null));
            addressBuilder.addressLine2(defaultIfBlank(oc.getAddress().getAddressLine2(), null));
            addressBuilder.addressLine3(defaultIfBlank(oc.getAddress().getAddressLine3(), null));
            addressBuilder.postTown(defaultIfBlank(oc.getAddress().getPostTown(), null));
            addressBuilder.postcode(defaultIfBlank(oc.getAddress().getPostcode(), null));
            addressBuilder.county(defaultIfBlank(oc.getAddress().getCounty(), null));
            addressBuilder.country(defaultIfBlank(oc.getAddress().getCountry(), null));
        }

        Address address = addressBuilder.build();

        TelephoneNumber solicitorTelephone;

        if (isEmpty(oc.getSocialWorkerTel())) {
            solicitorTelephone = null;
        } else {
            TelephoneNumber.TelephoneNumberBuilder telephoneNumberBuilder = TelephoneNumber.builder();
            telephoneNumberBuilder.telephoneNumber(defaultIfBlank(oc.getSocialWorkerTel(), null));
            solicitorTelephone = telephoneNumberBuilder.build();
        }

        ChildParty.ChildPartyBuilder partyBuilder = ChildParty.builder();
        partyBuilder.partyId(UUID.randomUUID().toString());

        if (!isEmpty(oc.getChildName())) {
            partyBuilder.firstName(splitName(oc.getChildName()).get(0));

            if (splitName(oc.getChildName()).size() > 1) {
                partyBuilder.lastName(splitName(oc.getChildName()).get(1));
            }
        }

        partyBuilder.dateOfBirth(defaultIfBlank(oc.getChildDOB(), null));
        partyBuilder.address(address);
        partyBuilder.gender(defaultIfBlank(oc.getChildGender(), null));
        partyBuilder.genderIdentification(defaultIfBlank(oc.getChildGenderIdentification(), null));
        partyBuilder.livingSituation(defaultIfBlank(oc.getLivingSituation(), null));
        partyBuilder.situationDetails(defaultIfBlank(oc.getSituationDetails(), null));
        partyBuilder.situationDate(defaultIfBlank(oc.getSituationDate(), null ));
        partyBuilder.keyDates(defaultIfBlank(oc.getKeyDates(), null));
        partyBuilder.careAndContactPlan(defaultIfBlank(oc.getCareAndContact(), null));
        partyBuilder.adoption(defaultIfBlank(oc.getAdoption(), null));
        partyBuilder.placementOrderApplication(defaultIfBlank(oc.getPlacementOrderApplication(), null));
        partyBuilder.placementCourt(defaultIfBlank(oc.getPlacementCourt(), null));
        partyBuilder.mothersName(defaultIfBlank(oc.getMothersName(), null));
        partyBuilder.fathersName(defaultIfBlank(oc.getFathersName(), null));
        partyBuilder.fathersResponsibility(defaultIfBlank(oc.getFathersResponsibility(), null));
        partyBuilder.socialWorkerName(defaultIfBlank(oc.getSocialWorkerName(), null));
        partyBuilder.socialWorkerTel(solicitorTelephone);
        partyBuilder.additionalNeeds(defaultIfBlank(oc.getAdditionalNeeds(), null));
        partyBuilder.additionalNeedsDetails(defaultIfBlank(oc.getAdditionalNeedsDetails(), null));
        partyBuilder.detailsHidden(defaultIfBlank(oc.getDetailsHidden(), null));
        partyBuilder.detailsHiddenReason(defaultIfBlank(oc.getDetailsHiddenReason(), null));
        partyBuilder.litigationIssues(defaultIfBlank(oc.getLitigationIssues(), null));
        partyBuilder.litigationIssuesDetails(defaultIfBlank(oc.getLitigationIssuesDetails(), null));
        ChildParty party = partyBuilder.build();

        return Child.builder()
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
