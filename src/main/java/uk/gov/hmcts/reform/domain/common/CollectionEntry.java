package uk.gov.hmcts.reform.domain.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollectionEntry<T> {
    private final String id;
    private final T value;

    @JsonCreator
    public CollectionEntry(@JsonProperty("id") String id,
                           @JsonProperty("value") T value) {
        this.id = id;
        this.value = value;
    }
}
