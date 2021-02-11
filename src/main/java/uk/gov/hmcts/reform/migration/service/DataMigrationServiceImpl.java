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
        1604665756707715L, 1605864027624648L, 1604698798063326L, 1601466075213425L, 1606835150877657L,
        1600924712703100L, 1602695126592366L, 1610448987787679L, 1604057238263139L, 1607650998329954L,
        1602072210447867L, 1607089490115787L, 1605252849637867L);

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
