package uk.gov.hmcts.reform.migration.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class DataMigrationServiceImpl implements DataMigrationService<Object> {

    private static Map<String, String> CODE_MAPPINGS = new HashMap<>();

    static {
        CODE_MAPPINGS.put("SA", "384CVB9");     //swansea.gov.uk
        CODE_MAPPINGS.put("CDF", "H2KT533");    //cardiff.gov.uk
        CODE_MAPPINGS.put("SCC", "4NNLRCF");    //southampton.gov.uk
        CODE_MAPPINGS.put("PCC", "OFPSP1B");    //portsmouthcc.gov.uk
        CODE_MAPPINGS.put("STF", "WBU4ESU");    //staffordshire.gov.uk
        CODE_MAPPINGS.put("SN", "QAP9QOO");     //swindon.gov.uk
        CODE_MAPPINGS.put("SNW", "0HG9IC3");    //wiltshire.gov.uk
        CODE_MAPPINGS.put("RCC", "R3RPZ2J");    //reading.gov.uk
        CODE_MAPPINGS.put("HN", "S5NGS8K");     //hillingdon.gov.uk
        CODE_MAPPINGS.put("NLC", "V5N1XU9");    //northlincs.gov.uk
        CODE_MAPPINGS.put("LCC", "7N4APPH");    //liverpool.gov.uk
        CODE_MAPPINGS.put("KBC", "VMQXA7Y");    //knowsley.gov.uk
        CODE_MAPPINGS.put("RCT", "68MNZN8");    //rctcbc.gov.uk
        CODE_MAPPINGS.put("MTC", "0QKCVUO");    //merthyr.gov.uk
        CODE_MAPPINGS.put("NCC", "2AXXWN2");    //newcastle.gov.uk
        CODE_MAPPINGS.put("OCC", "YHOLEG4");    //oxfordshire.gov.uk
        CODE_MAPPINGS.put("FPLA", "CW6Z9FP");   //hmcts.net FPLA
        CODE_MAPPINGS.put("DCC", "L2HRO6S");    //durham.gov.uk
        CODE_MAPPINGS.put("LEI", "CVPRECR");    //leicester.gov.uk
        CODE_MAPPINGS.put("LCO", "7ODR8WA");    //leics.gov.uk
        CODE_MAPPINGS.put("DBC", "FB91I2T");    //darlington.gov.uk
        CODE_MAPPINGS.put("HCC", "R4BS28U");    //hertfordshire.gov.uk
        CODE_MAPPINGS.put("SOT", "RJ8XH43");    //stockton.gov.uk
        CODE_MAPPINGS.put("BDB", "PLJGOWT");    //blackburn.gov.uk
        CODE_MAPPINGS.put("SOC", "2UREHXF");    //somerset.gov.uk
        CODE_MAPPINGS.put("DER", "YVZ8FTN");    //derby.gov.uk
        CODE_MAPPINGS.put("LAN", "T9AYD9Q");    //lancashire.gov.uk
        CODE_MAPPINGS.put("LIC", "Z18723Z");    //lincolnshire.gov.uk
        CODE_MAPPINGS.put("TAM", "QVNOR47");    //tameside.gov.uk
        CODE_MAPPINGS.put("MWC", "SNCQEJO");    //medway.gov.uk
        CODE_MAPPINGS.put("WMD", "T1GWH7Y");    //wakefield.gov.uk
        CODE_MAPPINGS.put("NGC", "5QHS5TB");    //nottscc.gov.uk
        CODE_MAPPINGS.put("WSC", "QRI841X");    //worcestershire.gov.uk
        CODE_MAPPINGS.put("HFC", "6Z4Z9B3");    //herefordshire.gov.uk
        CODE_MAPPINGS.put("OLC", "QN2PU45");    //oldham.gov.uk
        CODE_MAPPINGS.put("STC", "18D9WP4");    //stockport.gov.uk
        CODE_MAPPINGS.put("DMC", "T4VVSTN");    //dudley.gov.uk
        CODE_MAPPINGS.put("WCC", "3MQKB0N");    //warwickshire.gov.uk
        CODE_MAPPINGS.put("MDB", "HCAP78N");    //middlesbrough.gov.uk
        CODE_MAPPINGS.put("BIR", "V6F3RO4");    //birminghamchildrenstrust.co.uk

// NOT IN PRD
//        CODE_MAPPINGS.put("NELC", null);        //nelincs.gov.uk
//        CODE_MAPPINGS.put("ERY", null);         //eastriding.gov.uk
//        CODE_MAPPINGS.put("BSC", null);         //buckinghamshire.gov.uk
//        CODE_MAPPINGS.put("MKC", null);         //milton-keynes.gov.uk
//        CODE_MAPPINGS.put("CEB", null);         //cheshireeast.gov.uk
//        CODE_MAPPINGS.put("NHC", null);         //nottinghamcity.gov.uk
//        CODE_MAPPINGS.put("DSC", null);         //derbyshire.gov.uk
//        CODE_MAPPINGS.put("CCC", null);         //coventry.gov.uk
//        CODE_MAPPINGS.put("BCP", null);         //bcpcouncil.gov.uk
    }

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails ->
            !"Deleted".equals(caseDetails.getState()) &&
                Optional.ofNullable(caseDetails)
                    .map(CaseDetails::getData)
                    .filter(data -> "KBC".equals(data.get("caseLocalAuthority")) )
                    .filter(data -> !data.containsKey("localAuthorityPolicy"))
                    .isPresent();
    }

    @Override
    public Object migrate(Map<String, Object> data) {
        Map<String, Object> updates = new HashMap<>();

        String localAuthorityCode = (String) data.get("caseLocalAuthority");

        if (localAuthorityCode == null) {
            throw new IllegalStateException("LocalAuthority not specified");
        }

        String organisationCode = CODE_MAPPINGS.get(localAuthorityCode);

        if (organisationCode == null) {
            throw new IllegalStateException("Organisation mapping not found for " + localAuthorityCode);
        }

        updates.put("localAuthorityPolicy", organisationPolicy(organisationCode));

        return updates;
    }

    private Map<String, Object> organisationPolicy(String organisationCode) {
        Map<String, Object> organisationPolicy = new HashMap<>();
        organisationPolicy.put("OrgPolicyCaseAssignedRole", "[LASOLICITOR]");
        organisationPolicy.put("Organisation", Map.of("OrganisationID", organisationCode));

        return organisationPolicy;
    }
}
