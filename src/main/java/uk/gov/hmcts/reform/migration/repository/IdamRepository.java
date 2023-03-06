package uk.gov.hmcts.reform.migration.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.domain.exception.AuthenticationException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;

@Repository
@Slf4j
public class IdamRepository {

    private final IdamClient idamClient;

    private final String idamUsername;

    private final String idamPassword;

    private final OAuth2Configuration oauth2Configuration;

    @Autowired
    public IdamRepository(@Value("${migration.idam.username}") String idamUsername,
                          @Value("${migration.idam.password}") String idamPassword,
                          IdamClient idamClient,
                          OAuth2Configuration oauth2Configuration
    ) {
        this.idamUsername = idamUsername;
        this.idamPassword = idamPassword;
        this.idamClient = idamClient;
        this.oauth2Configuration = oauth2Configuration;
    }

    public String generateUserToken() {
        if (idamUsername == null || idamUsername.isBlank()) {
            throw new AuthenticationException("idam.username property can't be empty");
        }
        if (idamPassword == null || idamPassword.isBlank()) {
            throw new AuthenticationException("idam.password property can't be empty");
        }
        log.info("Authenticating user name {}", idamUsername);
        log.info("Authenticating password {}", idamPassword);
        log.info("IdamClient clientid {}", oauth2Configuration.getClientId());
        log.info("IdamClient RedirectUri {}", oauth2Configuration.getRedirectUri());
        log.info("IdamClient clientSecret {}", oauth2Configuration.getClientSecret());
        return idamClient.authenticateUser(idamUsername, idamPassword);
    }
}
