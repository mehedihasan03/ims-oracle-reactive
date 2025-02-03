package net.celloscope.mraims.loanportfolio.auth;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    @Value("${validate.token.url}")
    private String validateTokenUrl;

    private final RestTemplate restTemplate;

    public TokenValidationResponse validateJwtTokenByApiUrl(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    validateTokenUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> responseData = responseEntity.getBody();
            log.info("Token validation response: {}", responseData);

            if (responseData != null && (int) responseData.getOrDefault("code", 0) == 200) {
                return new TokenValidationResponse(true, responseData.get("data"));
            }

            log.warn("Token validation failed. Response: {}", responseData);
            return new TokenValidationResponse(false, null);
        } catch (HttpClientErrorException e) {
            log.error("HTTP error during token validation: {}", e.getMessage(), e);
            return new TokenValidationResponse(false, null);
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage(), e);
            return new TokenValidationResponse(false, null);
        }
    }

    public record TokenValidationResponse(boolean isValid, @Getter Object credentials) {
    }
}

