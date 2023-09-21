package uk.gov.hmcts.reform.migration.repository;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.JudicialUserRequest;
import uk.gov.hmcts.reform.migration.JudicialUserProfile;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "rd-judicial-api",
    url = "${rd_judicial.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface JudicialApi {

    @PostMapping("/refdata/judicial/users")
    List<JudicialUserProfile> findUsers(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader("page_size") int pageSize,
        @RequestBody JudicialUserRequest request
    );

}
