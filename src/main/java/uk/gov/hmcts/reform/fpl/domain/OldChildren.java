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
public class OldChildren {
    private final OldChild firstChild;
    private final List<CollectionEntry<OldChild>> additionalChildren;

    @JsonIgnore
    public List<CollectionEntry<OldChild>> getAll() {
        return ImmutableList.<CollectionEntry<OldChild>>builder()
            .add(CollectionEntry.<OldChild>builder().value(firstChild).build())
            .addAll(additionalChildren)
            .build();
    }
}
