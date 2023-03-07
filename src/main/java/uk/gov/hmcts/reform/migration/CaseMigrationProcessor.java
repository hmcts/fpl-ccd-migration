package uk.gov.hmcts.reform.migration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.domain.exception.CaseMigrationException;
import uk.gov.hmcts.reform.domain.exception.MigrationLimitReachedException;
import uk.gov.hmcts.reform.migration.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.migration.repository.ElasticSearchRepository;
import uk.gov.hmcts.reform.migration.repository.IdamRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static uk.gov.hmcts.reform.migration.service.DataMigrationService.MIGRATION_ID_KEY;

@Slf4j
@Component
public class CaseMigrationProcessor {
    public static final String EVENT_ID = "migrateCase";
    public static final String EVENT_SUMMARY = "Migrate Case";
    public static final String EVENT_DESCRIPTION = "Migrate Case";
    public static final String LOG_STRING = "-----------------------------------------";

    private final CoreCaseDataService coreCaseDataService;
    private final ElasticSearchRepository elasticSearchRepository;
    private final IdamRepository idamRepository;
    private final int caseProcessLimit;
    private final int defaultThreadLimit;
    private final int defaultQuerySize;
    private final String migrationId;

    @Getter
    private final List<Long> migratedCases = new ArrayList<>();
    @Getter
    private final List<Long> failedCases = new ArrayList<>();
    @Getter
    private final List<Long> ignoredCases = new ArrayList<>();
    private final LocalDateTime startTime = LocalDateTime.now();

    //@Autowired
    public CaseMigrationProcessor(CoreCaseDataService coreCaseDataService,
                                  ElasticSearchRepository elasticSearchRepository,
                                  IdamRepository idamRepository,
                                  @Value("${case-migration.processing.limit}") int caseProcessLimit,
                                  @Value("${default.thread.limit}") int defaultThreadLimit,
                                  @Value("${default.query.size}") int defaultQuerySize,
                                  @Value("${case-migration.processing.id}") String migrationId) {
        this.coreCaseDataService = coreCaseDataService;
        this.elasticSearchRepository = elasticSearchRepository;
        this.idamRepository = idamRepository;
        this.caseProcessLimit = caseProcessLimit;
        this.defaultThreadLimit = defaultThreadLimit;
        this.defaultQuerySize = defaultQuerySize;
        this.migrationId = migrationId;
    }


    public void process(String caseType) throws InterruptedException {
        try {
            validateMigrationId();
            validateCaseType(caseType);
            log.info("Data migration of cases started for case type: {}", caseType);
            log.info("Data migration of cases started for defaultThreadLimit: {} defaultQuerySize : {}",
                     defaultThreadLimit, defaultQuerySize);
            String userToken =  idamRepository.generateUserToken();

            SearchResult searchResult = elasticSearchRepository.fetchFirstPage(userToken, caseType, defaultQuerySize);

            log.info("Data migration required for cases {}", searchResult.getTotal());

            if (Objects.nonNull(searchResult) && searchResult.getTotal() > 0) {
                ExecutorService executorService = Executors.newFixedThreadPool(defaultThreadLimit);

                List<CaseDetails> searchResultCases = searchResult.getCases();
                searchResultCases
                    .forEach(submitMigration(userToken, caseType, executorService));
                String searchAfterValue = searchResultCases.get(searchResultCases.size() - 1).getId().toString();

                boolean keepSearching;
                do {
                    SearchResult subsequentSearchResult = elasticSearchRepository.fetchNextPage(userToken,
                                                                        caseType,
                                                                        searchAfterValue,
                                                                        defaultQuerySize);

                    log.info("Data migration of cases started for searchAfterValue : {}",searchAfterValue);

                    keepSearching = false;
                    if (Objects.nonNull(subsequentSearchResult)) {
                        List<CaseDetails> subsequentSearchResultCases = subsequentSearchResult.getCases();
                        subsequentSearchResultCases
                            .forEach(submitMigration(userToken, caseType, executorService));

                        keepSearching = subsequentSearchResultCases.size() > 0;
                        if (keepSearching) {
                            searchAfterValue = subsequentSearchResultCases
                                .get(subsequentSearchResultCases.size() - 1).getId().toString();
                        }
                    }
                } while (keepSearching);

                executorService.shutdown();
                boolean status = executorService.awaitTermination(4, TimeUnit.HOURS);
                log.info("Migration task finished with status {}", status);
            } else {
                log.error("No records found for case type {}", caseType);
            }
        } catch (MigrationLimitReachedException ex) {
            throw ex;
        } finally {
            publishStats(startTime);
        }
    }

    private Consumer<CaseDetails> submitMigration(String userToken, String caseType, ExecutorService executorService) {
        return caseDetails -> {
            log.info("Submitting task for migration of case  {}.", caseDetails.getId());
            executorService.submit(() -> updateCase(userToken, caseType, caseDetails));
        };
    }


    public void migrateCases(String caseType) {
        validateMigrationId();
        validateCaseType(caseType);
        log.info("Data migration of cases started for case type: {}", caseType);
        String userToken =  idamRepository.generateUserToken();
        List<CaseDetails> listOfCaseDetails = elasticSearchRepository.findCaseByCaseType(userToken, caseType);
        listOfCaseDetails.stream()
            .limit(caseProcessLimit)
            .forEach(caseDetails -> updateCase(userToken, caseType, caseDetails));
        publishStats(startTime);
    }

    private void publishStats(LocalDateTime startTime) {
        log.info(LOG_STRING);
        log.info(
            " FPLA Data migration completed: Total number of processed cases: {}",
            getMigratedCases().size() + getFailedCases().size() + getIgnoredCases().size()
        );

        String[] task = {"Migrated", "migrations"};
        if ("DFPL-1124Rollback".equals(migrationId)) {
            task = new String[]{"Rolled back", "rollbacks"};
        }

        if (getMigratedCases().isEmpty()) {
            log.info("{} cases: NONE ", task[0]);
        } else {
            log.info(
                " Total number of {} performed: {} ",
                task[1],
                getMigratedCases().size()
            );
        }

        if (getIgnoredCases().isEmpty()) {
            log.info("Ignored cases: NONE ");
        } else {
            log.info("Ignored cases: {} ", getIgnoredCases());

        }

        if (getFailedCases().isEmpty()) {
            log.info("Failed cases: NONE ");
        } else {
            log.info("Failed cases: {} ", getFailedCases());
        }

        log.info("Data migration start at {} and completed at {}", startTime, LocalDateTime.now());
    }

    private void validateMigrationId() {
        Optional.ofNullable(migrationId)
            .orElseThrow(() -> new CaseMigrationException("MigrationId is not set"));
    }

    private void validateCaseType(String caseType) {
        Optional.ofNullable(caseType)
            .orElseThrow(() -> new CaseMigrationException("Provide case type for the migration"));

        if (caseType.split(",").length > 1) {
            throw new CaseMigrationException("Only One case type at a time is allowed for the migration");
        }
    }


    private void updateCase(String authorisation, String caseType, CaseDetails caseDetails) {
        if (Objects.nonNull(caseDetails)) {
            Long id = caseDetails.getId();
            log.info("Updating case {}", id);
            try {
                caseDetails.getData().put(MIGRATION_ID_KEY, migrationId);
                CaseDetails updateCaseDetails = coreCaseDataService.update(
                    authorisation,
                    EVENT_ID,
                    EVENT_SUMMARY,
                    EVENT_DESCRIPTION,
                    caseType,
                    caseDetails
                );

                if (updateCaseDetails != null) {
                    log.info("Case {} successfully updated", id);
                    migratedCases.add(id);
                } else {
                    log.info("Case {} ignored", id);
                    ignoredCases.add(id);
                }
            } catch (Exception e) {
                log.error("Case {} update failed due to : {}", id, e);
                failedCases.add(id);
            }
        } else {
            log.error("Case details not found for case type {}", caseType);
        }
    }
}
