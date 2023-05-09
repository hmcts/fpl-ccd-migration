package uk.gov.hmcts.reform.migration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.migration.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.migration.query.EsQuery;
import uk.gov.hmcts.reform.migration.repository.ElasticSearchRepository;
import uk.gov.hmcts.reform.migration.repository.IdamRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.math.RoundingMode.UP;
import static java.time.LocalDateTime.now;
import static java.util.Objects.requireNonNull;

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
    private final int defaultQuerySize;
    private final String migrationId;

    @Getter
    private final List<Long> migratedCases = new ArrayList<>();
    @Getter
    private final List<Long> failedCases = new ArrayList<>();
    private final LocalDateTime startTime = now();

    //@Autowired
    public CaseMigrationProcessor(CoreCaseDataService coreCaseDataService,
                                  ElasticSearchRepository elasticSearchRepository,
                                  IdamRepository idamRepository,
                                  @Value("${default.query.size}") int defaultQuerySize,
                                  @Value("${case-migration.processing.id}") String migrationId) {
        this.coreCaseDataService = coreCaseDataService;
        this.elasticSearchRepository = elasticSearchRepository;
        this.idamRepository = idamRepository;
        this.defaultQuerySize = defaultQuerySize;
        this.migrationId = migrationId;
    }

    public void migrateList(String caseType, String jurisdiction, List<String> caseIds) {
        requireNonNull(caseType);
        requireNonNull(jurisdiction);
        requireNonNull(caseIds);

        if (caseIds.isEmpty()) {
            log.error("No case ids found for migration {}, aborting", migrationId);
            return;
        }

        String userToken =  idamRepository.generateUserToken();
        log.info("Found {} cases to migrate", caseIds.size());

        caseIds.parallelStream().forEach(caseId -> {
            long caseIdL = Long.parseLong(caseId);
            try {
                coreCaseDataService.update(userToken,
                    EVENT_ID,
                    EVENT_SUMMARY,
                    EVENT_DESCRIPTION,
                    caseType,
                    CaseDetails.builder()
                        .id(caseIdL)
                        .jurisdiction(jurisdiction)
                        .build(),
                    this.migrationId);
                log.info("Completed migrating case {}", caseId);
                migratedCases.add(caseIdL);
            } catch (Exception e) {
                log.error("Failed migrating case {}", caseId);
                failedCases.add(caseIdL);
            }
        });

        publishStats(startTime);
    }

    public void migrateCases(String caseType, EsQuery query) {
        requireNonNull(caseType);
        requireNonNull(query);
        requireNonNull(migrationId);

        String userToken =  idamRepository.generateUserToken();

        int total;

        try {
            total = elasticSearchRepository.searchResultsSize(userToken, caseType, query);
            log.info("Found {} cases to migrate", total);
        } catch (Exception e) {
            log.error("Could not determine the number of cases to search for due to {}",
                e.getMessage(), e
            );
            log.info("Migration finished unsuccessfully.");
            return;
        }

        int pages = paginate(total);
        log.debug("Found {} pages", pages);
        IntStream.range(0, pages).forEach(i -> {
            try {
                List<CaseDetails> cases = elasticSearchRepository.search(userToken, caseType, query,
                    defaultQuerySize, i * defaultQuerySize);

                cases.parallelStream().forEach(caseDetails -> {
                    try {
                        coreCaseDataService.update(userToken,
                            EVENT_ID,
                            EVENT_SUMMARY,
                            EVENT_DESCRIPTION,
                            caseType,
                            caseDetails,
                            this.migrationId
                        );
                        log.info("Completed migrating case {}", caseDetails.getId());
                        migratedCases.add(caseDetails.getId());
                    } catch (Exception e) {
                        failedCases.add(caseDetails.getId());
                    }
                });
            } catch (Exception e) {
                log.error("Migration could not search for cases on page {} due to {}", i, e.getMessage(), e);
            }
        });

        publishStats(startTime);
    }

    private int paginate(int total) {
        return new BigDecimal(total).divide(new BigDecimal(defaultQuerySize), UP).intValue();
    }

    private void publishStats(LocalDateTime startTime) {
        log.info(LOG_STRING);
        log.info(
            "FPLA Data migration completed: Total number of processed cases: {}",
            getMigratedCases().size() + getFailedCases().size()
        );

        String[] task = {"Migrated", "migrations"};
        if ("DFPL-1124Rollback".equals(migrationId)) {
            task = new String[]{"Rolled back", "rollbacks"};
        }

        if (getMigratedCases().isEmpty()) {
            log.info("{} cases: NONE ", task[0]);
        } else {
            log.info(
                "Total number of {} performed: {} ",
                task[1],
                getMigratedCases().size()
            );
        }

        if (getFailedCases().isEmpty()) {
            log.info("Failed cases: NONE ");
        } else {
            log.info("Failed count:{}, cases: {} ", getFailedCases().size(), getFailedCases());
        }

        log.info("Data migration start at {} and completed at {}", startTime, now());
    }
}
