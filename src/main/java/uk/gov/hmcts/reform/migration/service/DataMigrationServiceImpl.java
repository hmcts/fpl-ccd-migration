package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {

    final List<Long> caseReferences = List.of(
        1602684664094829L, 1611162463937927L, 1603112430374845L, 1609332375073780L, 1606920447453254L,
        1603186601101963L, 1601047439875628L, 1607700990753436L, 1593751852182152L, 1599567482297796L,
        1600763666484840L, 1608550125506632L, 1611073519965954L, 1601036838115652L, 1600681449523496L);

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .filter(caseData -> caseReferences.contains(caseData.getId()))
            .isPresent();
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return Map.<String, Object>of("migrationId", "FPLA-2710");
    }
}
