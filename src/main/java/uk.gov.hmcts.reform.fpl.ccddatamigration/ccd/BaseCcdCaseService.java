package uk.gov.hmcts.reform.fpl.ccddatamigration.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.fpl.ccddatamigration.s2s.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;


class BaseCcdCaseService {
    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    UserDetails getUserDetails(String userToken) {
        return idamClient.getUserDetails(getBearerUserToken(userToken));
    }

    String getBearerUserToken(String userToken) {
        return AuthUtil.getBearToken(userToken);
    }

    String getServiceAuthToken() {
        return authTokenGenerator.generate();
    }
}
