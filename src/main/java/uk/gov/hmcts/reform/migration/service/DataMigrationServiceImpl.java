package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {
    List<Long> caseIds = List.of(1598429153622508L,
        1615191831533551L,
        1594384486007055L,
        1601977974423857L,
        1615571327261140L,
        1615476016828466L,
        1616507805759840L,
        1610015759403189L,
        1615994076934396L,
        1611613172339094L,
        1612440806991994L,
        1607004182103389L,
        1617045146450299L,
        1612433400114865L,
        1615890702114702L,
        1610018233059619L);

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> caseIds.contains(caseDetails.getId());
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return Map.<String, Object>of("migrationId", "FPLA-2982");
    }
}