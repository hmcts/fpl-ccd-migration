package uk.gov.hmcts.reform.fpl.ccddatamigration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CcdDataMigrationApplication {

    public static void main(String[] args) {

        System.setProperty("http.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("https.proxyPort", "8080");

        SpringApplication.run(CcdDataMigrationApplication.class, args).close();
    }
}
