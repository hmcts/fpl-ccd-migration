package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .map(CaseDetails::getData)
            .filter(data -> isMigrationRequiredForCase(data))
            .isPresent();

    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return Map.<String, Object>of("migrationId", "FPLA-2379");
    }

    private boolean isMigrationRequiredForCase(Map<String, Object> data) {
        Set<String> allCaseDataKeys = data.keySet();
        for (String key : allCaseDataKeys) {
            // If there is any one document found, then the case will be sent for data migration
            if (key.startsWith("documents_")) {
                // This document type is ArrayList and remaining are Maps, hence different casting has been applied
                if (key.equals("documents_socialWorkOther")) {
                    ArrayList value = (ArrayList) data.get(key);
                    if (value.size() > 0) {
                        return true;
                    }
                } else {
                    Map<String, Object> value = (Map<String, Object>) data.get(key);
                    if (value.size() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
