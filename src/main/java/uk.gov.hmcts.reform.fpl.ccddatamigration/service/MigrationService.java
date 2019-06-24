package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private CoreCaseDataApi ccdApi;

    @Getter
    private List<Long> migratedCases = new ArrayList<>();

    @Getter
    private List<Long> failedCases = new ArrayList<>();

    @Value("${log.debug}")
    private boolean debugEnabled;

    @Value("${ccd.dryrun}")
    private boolean dryRun;

    private static Predicate<CaseDetails> accepts() {
        return caseDetails -> caseDetails != null && caseDetails.getData() != null;
    }

    public void processSingleCase(String userToken, String s2sToken, String ccdCaseId) {
        CaseDetails aCase;
        try {
            aCase = ccdApi.getCase(userToken, s2sToken, ccdCaseId);
            log.info("case data {} ", aCase);

            // ADD CHECK FOR NEW CASE DATA STRUCTURE
            if (aCase != null && aCase.getData() != null) {
                updateOneCase(userToken, aCase);
            } else {
                log.info("Case {} already migrated.", aCase.getId());
            }
        } catch (Exception ex) {
            log.error("case Id {} not found, {}", ccdCaseId, ex.getMessage());
        }
    }

    public void processAllTheCases(String userToken, String s2sToken, String userId,
                                   String jurisdiction, String caseType) {
        Map<String, String> searchCriteria = new HashMap<>();
        int numberOfPages = requestNumberOfPage(userToken, s2sToken, userId, jurisdiction, caseType, searchCriteria);

        if (dryRun) {
            log.info("dryRun for one case ...");
            dryRunWithOneCase(userToken, s2sToken, userId, jurisdiction, caseType, numberOfPages);

        } else {
            log.info("migrating all the cases ...");
            IntStream.rangeClosed(1, numberOfPages)
                .forEach(page -> migrateCasesForPage(userToken, s2sToken, userId,
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
                updateOneCase(userToken, casesForPage.get(0));
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
            .forEach(cd -> updateOneCase(userToken, cd));
    }


    private void updateOneCase(String authorisation, CaseDetails cd) {
        String caseId = cd.getId().toString();
        if (debugEnabled) {
            log.info("updating case with id :" + caseId);
        }
        try {
            updateCase(authorisation, cd);
            if (debugEnabled) {
                log.info(caseId + " updated!");
            }
            migratedCases.add(cd.getId());
        } catch (Exception e) {
            log.error("update failed for case with id [{}] with error [{}] ", cd.getId().toString(),
                e.getMessage());

            failedCases.add(cd.getId());
        }
    }

    private void updateCase(String authorisation, CaseDetails cd) {
        String caseId = cd.getId().toString();
        Object data = cd.getData();
        if (debugEnabled) {
            log.info("data {}", data.toString());
        }
        coreCaseDataService.update(authorisation, caseId,
            EVENT_ID, EVENT_SUMMARY, EVENT_DESCRIPTION, data
        );
    }
}
