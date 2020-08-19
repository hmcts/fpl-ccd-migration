package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .stream()
            .filter(e -> e.getData().containsKey("familyManCaseNumber"))
            .anyMatch(e -> e.getData().containsValue("mockcaseID"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object migrate(Map<String, Object> data) {
        Object orderCollection = data.get("orderCollection");
        List<Object> list = ((ArrayList) orderCollection);
        list.remove(4);
        return data;
    }
}
