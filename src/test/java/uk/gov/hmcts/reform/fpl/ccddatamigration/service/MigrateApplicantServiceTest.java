package uk.gov.hmcts.reform.fpl.ccddatamigration.service;

import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.OldApplicant;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrateApplicantServiceTest {

    @InjectMocks
    private final MigrateApplicantService service = new MigrateApplicantService();

    @Test
    public void mapOldApplicantToNewApplicant() {
        Map<String, Object> data = new HashMap<>();

        OldApplicant applicant = OldApplicant.builder()
            .name("Applicant Name")
            .email("")
            .mobile("")
            .address(Address.builder()
                .county("")
                .country("")
                .postcode("")
                .postTown("")
                .addressLine1("address")
                .addressLine2("")
                .addressLine3("")
                .county("")
                .build())
            .jobTitle("")
            .telephone("")
            .personToContact("")
            .build();

        data.put("applicant", applicant);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1111L)
            .data(data)
            .build();

        CaseDetails caseDetails1 = service.migrateCase(caseDetails);

        System.out.println("caseDetails1 = " + caseDetails1);

        assertThat(caseDetails1.getData()).containsKeys("applicantNew");

    }
}
