package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.NewRespondent;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.Respondent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class MigrateRespondentService {

    private ObjectMapper objectMapper = new ObjectMapper();

    private String firstName;
    private String lastName;

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

    // Will be old case -> new case. For now oldRespondent -> newRespondent

    // Give method a Respondent object and it will return the new data structure
    private List<Map<String, Object>> migrateRespondents(Map<String, Object> respondents) {
        log.info("beginning to migrate respondents {}", respondents);
        /// FIRST RESPONDENT

        Respondent firstRespondent = objectMapper.convertValue(respondents.get("firstRespondent"), Respondent.class);
        NewRespondent migratedFirstRespondent = migrateIndividualRespondent(firstRespondent);

        /// ADDITIONAL RESPONDENT

        List<Map<String, Object>> additionalRespondents =
            (List<Map<String, Object>>) objectMapper.convertValue(respondents.get("additional"), List.class);

        List<NewRespondent> migratedRespondentCollection = additionalRespondents.stream()
            .map(respondent ->
                migrateIndividualRespondent(objectMapper.convertValue(respondent.get("value"), Respondent.class)))
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

    private NewRespondent migrateIndividualRespondent(Respondent or) {
        log.info("migrating respondent {}", or);

        if (or.getName() != null) {
            firstName = or.getName().split("\\s+")[0];
            lastName = or.getName().split("\\s+")[1];
        }

        return NewRespondent.builder()
            .partyType("Individual")
            .firstName(firstName)
            .lastName(lastName)
            .dateOfBirth("1111-11-11")
            .address(or.getAddress())
            .build();
    }
}
