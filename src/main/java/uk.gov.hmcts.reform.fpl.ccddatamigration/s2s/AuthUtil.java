package uk.gov.hmcts.reform.fpl.ccddatamigration.s2s;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("squid:S1118")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthUtil {

    private static final String BEARER = "Bearer ";

    public static String getBearToken(String token) {
        if (token == null || token.isBlank()) {
            return token;
        }

        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }
}

