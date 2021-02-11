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
        1609255930041507L, 1606987766244887L, 1604311638012517L, 1610617556504448L, 1611053637858331L,
        1611831571219051L, 1612440806991994L, 1611613172339094L, 1597234670803750L, 1610638275960711L,
        1612792241936277L, 1608227716601946L, 1606816433160806L, 1604488701821402L, 1612260529074989L);

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
