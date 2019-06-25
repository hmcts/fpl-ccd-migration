package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.ccd.CoreCaseDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
public class MigrationService {
    private static final String EVENT_ID = "migrateCase";
    private static final String EVENT_SUMMARY = "Migrate Case";
    private static final String EVENT_DESCRIPTION = "Migrate Case";

    @Autowired
    private CoreCaseDataService coreCaseDataService;

    @Getter
    private List<Long> migratedCases = new ArrayList<>();

    @Getter
    private List<Long> failedCases = new ArrayList<>();

    private static Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails != null && caseDetails.getData() != null;
    }

    public void processSingleCase(String userToken, String caseId) {
        CaseDetails caseDetails;
        try {
            caseDetails = coreCaseDataService.fetchOne(userToken, caseId);
        } catch (Exception ex) {
            log.error("Case {} not found due to: {}", caseId, ex.getMessage());
            return;
        }
        if (accepts().test(caseDetails)) {
            updateCase(userToken, caseDetails);
        } else {
            log.info("Case {} already migrated", caseDetails.getId());
        }
    }

    public void processAllCases(String userToken, String userId) {
        coreCaseDataService.fetchAll(userToken, userId).stream()
            .filter(accepts())
            .forEach(cd -> updateCase(userToken, cd));
    }

    private void updateCase(String authorisation, CaseDetails caseDetails) {
        log.info("Updating case {}", caseDetails.getId());
        try {
            log.debug("  case data: {}", caseDetails.getData());
            coreCaseDataService.update(authorisation, caseDetails.getId().toString(),
                EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, caseDetails.getData());
            log.info("Case {} successfully updated", caseDetails.getId());
            migratedCases.add(caseDetails.getId());
        } catch (Exception e) {
            log.error("Case {} update failed due to: {}", caseDetails.getId(), e.getMessage());
            failedCases.add(caseDetails.getId());
        }
    }
}
