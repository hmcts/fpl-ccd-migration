package uk.gov.hmcts.reform.fpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OldRespondents {
    private final OldRespondent firstRespondent;
    private final List<CollectionEntry<OldRespondent>> additional;

    @JsonIgnore
    public List<CollectionEntry<OldRespondent>> getAll() {
        return ImmutableList.<CollectionEntry<OldRespondent>>builder()
                .add(CollectionEntry.<OldRespondent>builder().value(firstRespondent).build())
                .addAll(additional)
                .build();
    }
}
