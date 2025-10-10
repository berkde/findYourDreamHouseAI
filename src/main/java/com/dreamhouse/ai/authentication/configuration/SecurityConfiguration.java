package com.dreamhouse.ai.authentication.configuration;

import com.dreamhouse.ai.authentication.exception.SecurityKeyException;
import com.dreamhouse.ai.house.service.SecretsService;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

import static io.jsonwebtoken.io.Decoders.BASE64;

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
    public SecretKey key() throws SecurityKeyException {
        String b64 = secretsService.getSecret(secretId, "jwt-secret");
        byte[] bytes = Decoders.BASE64.decode(b64);
        if (bytes.length != 32) throw new SecurityKeyException("A256GCM requires 32-byte key");
        return new javax.crypto.spec.SecretKeySpec(bytes, "AES");
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
