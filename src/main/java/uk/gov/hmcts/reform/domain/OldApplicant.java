package uk.gov.hmcts.reform.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.domain.common.Address;

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
        public OldApplicant( @JsonProperty("name") String name,
                             @JsonProperty("email") String email,
                             @JsonProperty("mobile") String mobile,
                             @JsonProperty("address") Address address,
                             @JsonProperty("jobTitle") String jobTitle,
                             @JsonProperty("telephone") String telephone,
                             @JsonProperty("personToContact") String personToContact,
                             @JsonProperty("pbaNumber") String pbaNumber) {

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
