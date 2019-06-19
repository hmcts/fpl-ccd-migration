package uk.gov.hmcts.reform.fpl.ccddatamigration.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;

import java.util.Date;

@Data
@Builder
public class OldRespondent {
    private final String name;
    private final String dob;
    private final String gender;
    private final String genderIdentify;
    private final String placeOfBirth;
    private final String telephone;
    private final String relationshipToChild;
    private final String contactDetailsHidden;
    private final String contactDetailsHiddenReason;
    private final String litigationIssues;
    private final String litigationIssuesDetails;
    private final Address address;

    @JsonCreator
    public OldRespondent(@JsonProperty("name") final String name,
                         @JsonProperty("dob") final String dob,
                         @JsonProperty("gender") final String gender,
                         @JsonProperty("genderIdentify") final String genderIdentify,
                         @JsonProperty("placeOfBirth") final String placeOfBirth,
                         @JsonProperty("telephone") final String telephone,
                         @JsonProperty("relationshipToChild") final String relationshipToChild,
                         @JsonProperty("contactDetailsHidden") final String contactDetailsHidden,
                         @JsonProperty("contactDetailsHiddenReason") final String contactDetailsHiddenReason,
                         @JsonProperty("litigationIssues") final String litigationIssues,
                         @JsonProperty("litigationIssuesDetails") final String litigationIssuesDetails,
                         @JsonProperty("address") final Address address) {
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.genderIdentify = genderIdentify;
        this.placeOfBirth = placeOfBirth;
        this.telephone = telephone;
        this.relationshipToChild = relationshipToChild;
        this.contactDetailsHidden = contactDetailsHidden;
        this.contactDetailsHiddenReason = contactDetailsHiddenReason;
        this.litigationIssues = litigationIssues;
        this.litigationIssuesDetails = litigationIssuesDetails;
        this.address = address;
    }
}
