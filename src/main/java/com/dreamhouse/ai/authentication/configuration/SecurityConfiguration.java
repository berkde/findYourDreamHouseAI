package com.dreamhouse.ai.authentication.configuration;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.dreamhouse.ai.authentication.exception.SecurityKeyException;
import com.dreamhouse.ai.cloud.service.SecretsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfiguration {
    private final SecretsService secretsService;
    private final String secretId;

    public SecurityConfiguration(
            SecretsService secretsService,
            @Value("${security.jwt.secret-id}") String secretId
    ) {
        this.secretsService = secretsService;
        this.secretId = secretId;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey secretKey(SecretCache cache) throws SecurityKeyException, JsonProcessingException {
        String secretJson = cache.getSecretString(secretId);
        String b64;

        if(secretJson != null && secretJson.trim().startsWith("{")){
            ObjectMapper mapper = new ObjectMapper();
            b64 = mapper.readTree(secretJson).path("jwt-secret").asText();
        } else {
            b64 = secretJson;
        }

        if (!StringUtils.hasText(b64)) {
            b64 = secretsService.getSecret(secretId, "jwt-secret");
        }

        byte[] raw = Decoders.BASE64.decode(b64.trim());
        if (raw.length != 64) {
            Arrays.fill(raw, (byte)0);
            throw new SecurityKeyException("A256CBC-HS512 requires 64 bytes; got " + raw.length);
        }
        try {
            return new SecretKeySpec(raw, "AES");
        } finally {
            Arrays.fill(raw, (byte)0);
        }
    }


    @Bean
    public UrlBasedCorsConfigurationSource corsConfiguration() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

}
