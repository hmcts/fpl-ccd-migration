package uk.gov.hmcts.reform.fpl.ccddatamigration.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.reform.fpl.ccddatamigration.s2s.AuthUtil.getBearerToken;

@Service
public class CoreCaseDataService {

    @Value("${ccd.jurisdiction}")
    private String jurisdiction;
    @Value("${ccd.casetype}")
    private String caseType;

    @Autowired
    private IdamClient idamClient;
    @Autowired
    private AuthTokenGenerator authTokenGenerator;
    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    public CaseDetails update(String authorisation, String caseId, String eventId, String eventSummary, String eventDescription, Object data) {
        UserDetails userDetails = idamClient.getUserDetails(getBearerToken(authorisation));

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            getBearerToken(authorisation),
            authTokenGenerator.generate(),
            userDetails.getId(),
            jurisdiction,
            caseType,
            caseId,
            eventId);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(eventSummary)
                    .description(eventDescription)
                    .build()
            ).data(data)
            .build();

        return coreCaseDataApi.submitEventForCaseWorker(
            getBearerToken(authorisation),
            authTokenGenerator.generate(),
            userDetails.getId(),
            jurisdiction,
            caseType,
            caseId,
            true,
            caseDataContent);
    }
}
