package uk.gov.hmcts.reform.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.JudicialUserRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.migration.configuration.CaseIdListConfiguration;
import uk.gov.hmcts.reform.migration.repository.IdamRepository;
import uk.gov.hmcts.reform.migration.repository.JudicialApi;
import uk.gov.hmcts.reform.migration.service.DataMigrationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@SpringBootApplication
@EnableFeignClients(value = {
    "uk.gov.hmcts.reform.migration.repository"
})
//@PropertySource("classpath:application.properties")
public class CaseMigrationRunner implements CommandLineRunner {

    @Autowired
    private CaseMigrationProcessor caseMigrationProcessor;
    @Autowired
    private DataMigrationService dataMigrationService;

    @Autowired
    private CaseIdListConfiguration caseIdListConfiguration;

    @Value("${case-migration.processing.id}") String migrationId;

    @Value("${case-migration.enabled}") boolean enabled;

    @Value("${case-migration.use_case_id_mapping:false}") boolean useIdList;

    public static void main(String[] args) {
        SpringApplication.run(CaseMigrationRunner.class, args);
    }

    @Autowired
    private JudicialApi judicialApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamRepository idamRepository;

    @Override
    public void run(String... args) {
        try {
            List<JudicialUserProfile> jups = judicialApi.findUsers(idamRepository.generateUserToken(),
                authTokenGenerator.generate(),
                10000,
                JudicialUserRequest.builder()
                    .ccdServiceName("PUBLICLAW")
                .build());

            Map<String, String> judges = jups.stream()
                .filter(jup -> !isEmpty(jup.getSidamId()))
                .collect(Collectors.toMap(profile -> profile.getEmailId().toLowerCase(),
                    JudicialUserProfile::getSidamId));

            log.info("Loaded {} jups", jups.size());
            log.info("Loaded {} judges", judges.size());

            log.info("JUDGES WITH IDS: {}", judges);
            log.info("ALL JUDGES: {}", jups.stream().map(JudicialUserProfile::getEmailId).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Migration failed with the following reason: {}", e.getMessage(), e);
        }
    }
}
