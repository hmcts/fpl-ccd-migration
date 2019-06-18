package uk.gov.hmcts.reform.fpl.ccddatamigration.idam;

public interface IdamUserClient {

    String generateUserTokenWithNoRoles(String username, String password);

}
