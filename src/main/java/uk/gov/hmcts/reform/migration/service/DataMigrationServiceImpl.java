package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {
    List<String> migratedListCodes = List.of("OT8", "OT9", "OT10");

    @Override
    public Predicate<CaseDetails> accepts() {
        return this::caseContainsMigratedCancellationReasons;
    }

    private boolean caseContainsMigratedCancellationReasons(CaseDetails caseDetails) {
        Optional<List<String>> cancellationReasonList = Optional.ofNullable(caseDetails)
            .map(CaseDetails::getData)
            .map(data -> data.get("cancelledHearingDetails"))
            .map(hearingList -> (List<Map<String, Object>>) hearingList)
            .map(hearingList -> hearingList.stream()
                .map(hearingListItem -> {
                    Map<String, Object> value = (Map<String, Object>) hearingListItem.get("value");
                    return (String) value.get("cancellationReason");
                }).collect(Collectors.toList()));

        List<String> cancellationReasons = cancellationReasonList.orElse(new ArrayList<>());

        return !Collections.disjoint(cancellationReasons, migratedListCodes);
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        return Map.<String, Object>of("migrationId", "FPLA-2885");
    }
}
