package com.dreamhouse.ai.authentication.configuration;

import com.dreamhouse.ai.authentication.repository.UserRepository;
import com.dreamhouse.ai.authentication.service.impl.UserServiceImpl;
import com.dreamhouse.ai.authentication.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurity {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserServiceImpl userService;
    private final UrlBasedCorsConfigurationSource corsConfigurationSource;
    private final SecretKey key;

    @Autowired
    public WebSecurity(BCryptPasswordEncoder passwordEncoder,
                       UserServiceImpl userService,
                       UrlBasedCorsConfigurationSource corsConfigurationSource,
                       SecretKey key) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.corsConfigurationSource = corsConfigurationSource;
        this.key = key;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           UserRepository userRepository,
                                           SecurityUtil securityUtil,
                                           ClientIpLoggingFilter clientIpLoggingFilter) throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationManager(authenticationManager(http))
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(HttpMethod.POST,"/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/houseAds").permitAll()
                                .anyRequest().authenticated())
                .addFilterBefore(clientIpLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(new AuthenticationFilter(authenticationManager(http), userService, userRepository, securityUtil, key),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new AuthorizationFilter(authenticationManager(http), key), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder manager = http.getSharedObject(AuthenticationManagerBuilder.class);
        manager.userDetailsService(userService).passwordEncoder(passwordEncoder);
        return manager.getOrBuild();
    }
}
