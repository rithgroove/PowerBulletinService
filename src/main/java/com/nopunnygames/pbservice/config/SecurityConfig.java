package com.nopunnygames.pbservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Development security configuration for local CMS and simulator integration.
 */
@Configuration
public class SecurityConfig {
    /**
     * Creates the security configuration.
     */
    public SecurityConfig() {
    }

    /**
     * Creates a permissive local security filter chain.
     *
     * @param http HTTP security builder
     * @return configured security filter chain
     * @throws Exception when Spring Security cannot build the chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
