package uk.gov.hmcts.reform.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;

import java.util.List;

@Data
@Builder
@JsonInclude
public class CaseData {
    private final OldApplicant applicant;
    private final List<CollectionEntry<Applicant>> applicants;

    public CaseData(OldApplicant applicant, List<CollectionEntry<Applicant>> applicants) {
        this.applicant = applicant;
        this.applicants = applicants;
    }
}
