package uk.gov.hmcts.reform.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CaseData {
    private final OldApplicant applicant;
    private final List<CollectionEntry<Applicant>> applicants;
}
