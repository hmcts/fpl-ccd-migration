package uk.gov.hmcts.reform.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.hmcts.reform.migration.query.EsQuery;
import uk.gov.hmcts.reform.migration.service.DataMigrationService;

@Slf4j
@SpringBootApplication
//@PropertySource("classpath:application.properties")
public class CaseMigrationRunner implements CommandLineRunner {

    @Autowired
    private CaseMigrationProcessor caseMigrationProcessor;
    @Autowired
    private DataMigrationService dataMigrationService;

    @Value("${case-migration.processing.id}") String migrationId;

    @Value("${case-migration.enabled}") boolean enabled;
    @Value("${case-migration.list:false}") boolean useIdList;

    @Value("${migration.caseType}")
    private String caseType;

    @Value("${default.thread.limit}")
    private int defaultThreadLimit;

    public static void main(String[] args) {
        SpringApplication.run(CaseMigrationRunner.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            log.info("Job is triggered: {}", enabled);
            if (!enabled) {
                return;
            }
            dataMigrationService.validateMigrationId(migrationId);
            if (useIdList) {
                // Do ID List Migration
                // TODO - write this logic
            } else {
                // Do ESQuery based migration
                EsQuery query = dataMigrationService.getQuery(migrationId);
                caseMigrationProcessor.migrateCases(caseType, query);
            }
        } catch (Exception e) {
            log.error("Migration failed with the following reason: {}", e.getMessage(), e);
        }
    }
}
