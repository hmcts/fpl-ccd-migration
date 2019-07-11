package uk.gov.hmcts.reform.domain.common;

public enum PartyType {
     INDIVIDUAL("Individual"),
     ORGANISATION("Organisation");
         
     private final String partyType;

     PartyType(String partyType) { this.partyType = partyType; }

     public String getPartyType() { return partyType; }
}
