package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .map(CaseDetails::getData)
            .filter(data -> data.getOrDefault("familyManCaseNumber", "").equals("CF20C50047"))
            .isPresent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object migrate(Map<String, Object> data) {
        List<Object> orderCollection = (ArrayList) data.get("orderCollection");
        orderCollection.remove(1);

        List<Map<String, Map<String, Object>>> children1 = (ArrayList) data.get("children1");

        for (Map<String, Map<String, Object>> child : children1) {
            Map<String, Object> value = child.get("value");
            value.remove("finalOrderIssued");
            value.remove("finalOrderIssuedType");
        }

        Map<String, Object> migration = new HashMap<String, Object>();
        migration.put("orderCollection", orderCollection);
        migration.put("children1", children1);
        migration.put("state", "PREPARE_FOR_HEARING");
        migration.put("closeCaseTabField", null);

        return migration;
    }
}
