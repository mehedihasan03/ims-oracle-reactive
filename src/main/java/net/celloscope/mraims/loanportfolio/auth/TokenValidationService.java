package net.celloscope.mraims.loanportfolio.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final JwtTokenUtil jwtTokenUtil;

    public AuthUser validateTokenAndExtractCredentialsByApiUrl(String authorizationHeader) {
        JwtTokenUtil.TokenValidationResponse response = jwtTokenUtil.validateJwtTokenByApiUrl(authorizationHeader);

        if (!response.isValid()) {
            throw new IllegalArgumentException("Token is invalid");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> credentials = (Map<String, Object>) response.getCredentials();

        return AuthUser.builder()
                .userId((String) credentials.get("user_id"))
                .userRole((String) credentials.get("role"))
                .schemaName((String) credentials.get("sname"))
                .mfiId((String) credentials.get("sid"))
                .build();
    }
}
