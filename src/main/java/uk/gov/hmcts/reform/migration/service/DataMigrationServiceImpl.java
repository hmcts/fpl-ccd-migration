package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {
    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails ->
            Optional.ofNullable(caseDetails)
                .map(CaseDetails::getData)
                .filter(data -> data.containsKey("localAuthorityPolicy"))
                .isPresent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object migrate(Map<String, Object> data) {
        Map<String, Object> organisationPolicy = (Map) data.get("localAuthorityPolicy");
        Map<String, Object> organisation = (Map) organisationPolicy.getOrDefault("Organisation", null);

        if (organisation != null) {
            organisation.remove("OrganisationName");
        }

        Map<String, Object> caseUpdate = new HashMap<>();
        caseUpdate.put("localAuthorityPolicy", organisationPolicy);

        return caseUpdate;
    }
}
