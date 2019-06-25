package uk.gov.hmcts.reform.fpl.ccddatamigration.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.ccddatamigration.domain.common.Address;

@Data
@Builder
public class OldApplicant {
        private final String name;
        private final String email;
        private final String mobile;
        private final Address address;
        private final String jobTitle;
        private final String telephone;
        private final String personToContact;
        private final String pbaNumber;

        @JsonCreator
        public OldApplicant( @JsonProperty("name") final String name,
                             @JsonProperty("Email") final String email,
                             @JsonProperty("mobile") final String mobile,
                             @JsonProperty("address") final Address address,
                             @JsonProperty("jobTitle") final String jobTitle,
                             @JsonProperty("telephone") final String telephone,
                             @JsonProperty("personToContact") final String personToContact,
                             @JsonProperty("pbaNumber") final String pbaNumber) {

            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.address = address;
            this.jobTitle = jobTitle;
            this.telephone = telephone;
            this.personToContact = personToContact;
            this.pbaNumber = pbaNumber;
        }
}
