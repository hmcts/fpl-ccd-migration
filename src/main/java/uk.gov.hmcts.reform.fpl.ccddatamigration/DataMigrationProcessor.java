package uk.gov.hmcts.reform.fpl.ccddatamigration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.fpl.ccddatamigration.service.MigrationService;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Slf4j
@SpringBootApplication
@PropertySource("classpath:application.properties")
public class DataMigrationProcessor implements CommandLineRunner {

    @Value("${idam.username}")
    private String idamUsername;

    @Value("${idam.password}")
    private String idamPassword;

    @Value("${ccd.jurisdiction}")
    private String jurisdiction;

    @Value("${ccd.casetype}")
    private String caseType;

    @Value("${ccd.caseId}")
    private String ccdCaseId;

    @Value("${log.debug}")
    private boolean debugEnabled;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    private MigrationService migrationService;

    public static void main(String[] args) {
        System.setProperty("http.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("https.proxyPort", "8080");

        SpringApplication.run(DataMigrationProcessor.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            if (debugEnabled) {
                log.info("Data migration started");
            }
            String userToken = idamClient.authenticateUser(idamUsername, idamPassword);
            if (debugEnabled) {
                log.info("  userToken: {}", userToken);
            }
            String userId = idamClient.getUserDetails(userToken).getId();
            if (debugEnabled) {
                log.info("  userId: {}", userId);
            }

            if (ccdCaseId != null && !ccdCaseId.isBlank()) {
                migrationService.processSingleCase(userToken, ccdCaseId);
            } else {
                migrationService.processAllCases(userToken, userId, jurisdiction, caseType);
            }

            log.info("Migrated cases: {} ", !migrationService.getMigratedCases().isEmpty() ? migrationService.getMigratedCases() : "NONE");
            log.info("Failed cases: {}", !migrationService.getFailedCases().isEmpty() ? migrationService.getFailedCases() : "NONE");

            log.info("-----------------------------");
            log.info("Data migration completed");
            log.info("-----------------------------");
            log.info("Total number of cases: {}", migrationService.getMigratedCases().size() + migrationService.getFailedCases().size());
            log.info("Total migrations performed: {}", migrationService.getMigratedCases().size());
            log.info("-----------------------------");
        } catch (Throwable e) {
            log.error("Migration failed with the following reason: {}", e.getMessage(), e);
        }
    }
}
