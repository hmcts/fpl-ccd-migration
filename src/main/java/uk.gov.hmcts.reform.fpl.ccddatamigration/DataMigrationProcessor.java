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

    @Value("${ccd.caseId}")
    private String ccdCaseId;

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
            String userToken = idamClient.authenticateUser(idamUsername, idamPassword);
            log.debug("  userToken: {}", userToken);
            String userId = idamClient.getUserDetails(userToken).getId();
            log.debug("  userId: {}", userId);

            if (ccdCaseId != null && !ccdCaseId.isBlank()) {
                log.info("Data migration of single case started");
                migrationService.processSingleCase(userToken, ccdCaseId);
            } else {
                log.info("Data migration of all cases started");
                migrationService.processAllCases(userToken, userId);
            }

            log.info("-----------------------------------------");
            log.info("Data migration completed");
            log.info("-----------------------------------------");
            log.info("Total number of processed cases: {}", migrationService.getMigratedCases().size() + migrationService.getFailedCases().size());
            log.info("Total number of migrations performed: {}", migrationService.getMigratedCases().size());
            log.info("-----------------------------------------");
            log.info("Migrated cases: {} ", !migrationService.getMigratedCases().isEmpty() ? migrationService.getMigratedCases() : "NONE");
            log.info("Failed cases: {}", !migrationService.getFailedCases().isEmpty() ? migrationService.getFailedCases() : "NONE");
        } catch (Throwable e) {
            log.error("Migration failed with the following reason: {}", e.getMessage(), e);
        }
    }
}
