package uk.gov.hmcts.reform.fpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.domain.common.CollectionEntry;

import java.util.List;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OldChildren {
    private final OldChild firstChild;
    private final List<CollectionEntry<OldChild>> additionalChildren;

    @JsonCreator
    public OldChildren(@JsonProperty("firstChild") OldChild firstChild,
                       @JsonProperty("additionalChildren") List<CollectionEntry<OldChild>> additionalChildren) {
        this.firstChild  = firstChild;
        this.additionalChildren = additionalChildren;
    }

    @JsonIgnore
    public List<CollectionEntry<OldChild>> getAll() {
        return ImmutableList.<CollectionEntry<OldChild>>builder()
            .add(CollectionEntry.<OldChild>builder().value(firstChild).build())
            .addAll(additionalChildren)
            .build();
    }
}
