package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.PaginatedSearchMetadata;
import uk.gov.hmcts.reform.fpl.ccddatamigration.ccd.CoreCaseDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class MigrationService {
    private static final String EVENT_ID = "migrateCase";
    private static final String EVENT_SUMMARY = "Migrate Case";
    private static final String EVENT_DESCRIPTION = "Migrate Case";

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private CoreCaseDataApi ccdApi;

    @Value("${log.debug}")
    private boolean debugEnabled;

    @Value("${ccd.dryrun}")
    private boolean dryRun;

    @Getter
    private List<Long> migratedCases = new ArrayList<>();

    @Getter
    private List<Long> failedCases = new ArrayList<>();

    private static Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails != null && caseDetails.getData() != null;
    }

    public void processSingleCase(String userToken, String ccdCaseId) {
        try {
            CaseDetails caseDetails = ccdApi.getCase(userToken, authTokenGenerator.generate(), ccdCaseId);
            log.info("case data {} ", caseDetails);

            if (accepts().test(caseDetails)) {
                updateCase(userToken, caseDetails);
            } else {
                log.info("Case {} already migrated.", caseDetails.getId());
            }
        } catch (Exception ex) {
            log.error("case Id {} not found, {}", ccdCaseId, ex.getMessage());
        }
    }

    public void processAllCases(String userToken, String userId,
                                String jurisdiction, String caseType) {
        int numberOfPages = requestNumberOfPage(userToken, authTokenGenerator.generate(), userId, jurisdiction, caseType, new HashMap<>());

        if (dryRun) {
            log.info("dryRun for one case ...");
            dryRunWithOneCase(userToken, authTokenGenerator.generate(), userId, jurisdiction, caseType, numberOfPages);

        } else {
            log.info("migrating all the cases ...");
            IntStream.rangeClosed(1, numberOfPages)
                .forEach(page -> migrateCasesForPage(userToken, authTokenGenerator.generate(), userId,
                    jurisdiction, caseType, page));
        }
    }

    private int requestNumberOfPage(String authorisation,
                                    String serviceAuthorisation,
                                    String userId,
                                    String jurisdiction,
                                    String caseType,
                                    Map<String, String> searchCriteria) {
        PaginatedSearchMetadata paginationInfoForSearchForCaseworkers = ccdApi.getPaginationInfoForSearchForCaseworkers(
            authorisation,
            serviceAuthorisation,
            userId,
            jurisdiction,
            caseType,
            searchCriteria);
        if (debugEnabled) {
            log.debug("Pagination>>" + paginationInfoForSearchForCaseworkers.toString());
        }
        return paginationInfoForSearchForCaseworkers.getTotalPagesCount();
    }

    private void dryRunWithOneCase(String userToken, String s2sToken, String userId,
                                   String jurisdiction, String caseType, int numberOfPages) {
        boolean found = false;
        for (int i = 1; i <= numberOfPages && !found; i++) {
            List<CaseDetails> casesForPage = getCasesForPage(userToken, s2sToken, userId,
                jurisdiction, caseType, i);
            if (casesForPage.size() > 0) {
                found = true;
                log.info("Migrating Case Id {} for the dryRun", casesForPage.get(0).getId());
                updateCase(userToken, casesForPage.get(0));
            }
        }
    }

    private List<CaseDetails> getCasesForPage(String userToken,
                                              String s2sToken,
                                              String userId,
                                              String jurisdiction,
                                              String caseType,
                                              int pageNumber) {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("page", String.valueOf(pageNumber));
        return ccdApi.searchForCaseworker(userToken, s2sToken, userId, jurisdiction, caseType, searchCriteria)
            .stream()
            .filter(accepts())
            .collect(Collectors.toList());
    }

    private void migrateCasesForPage(String userToken,
                                     String s2sToken,
                                     String userId,
                                     String jurisdiction,
                                     String caseType,
                                     int pageNumber) {
        getCasesForPage(userToken, s2sToken, userId, jurisdiction, caseType, pageNumber)
            .stream()
            .filter(accepts())
            .forEach(cd -> updateCase(userToken, cd));
    }

    private void updateCase(String authorisation, CaseDetails caseDetails) {
        if (debugEnabled) {
            log.info("updating case with id: {}", caseDetails.getId());
        }
        try {
            if (debugEnabled) {
                log.info("data: {}", caseDetails.getData());
            }
            coreCaseDataService.update(authorisation, caseDetails.getId().toString(),
                EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, caseDetails.getData());
            if (debugEnabled) {
                log.info("{} updated!", caseDetails.getId());
            }
            migratedCases.add(caseDetails.getId());
        } catch (Exception e) {
            log.error("update failed for case with id [{}] with error [{}] ", caseDetails.getId(), e.getMessage());
            failedCases.add(caseDetails.getId());
        }
    }

}
