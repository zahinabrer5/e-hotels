package org.uncreatives.e_hotels.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> { // allow requests from frontend
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOriginPatterns(java.util.Collections.singletonList("*"));
                config.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(java.util.Arrays.asList("*"));
                return config;
            }))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()

                // Needed for CORS
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // Allow public access to view the rooms, statistics, and views directly from the website UI
                .requestMatchers("/api/management/search-rooms").permitAll()
                .requestMatchers("/api/management/views/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/management/customers").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                
                // Allow public access to Swagger UI & OpenAPI Docs
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Customer can create Booking
                // Employee can turn Booking to Renting or directly make a Renting
                .requestMatchers("/api/management/book").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_EMPLOYEE")
                .requestMatchers("/api/management/rent").hasAuthority("ROLE_EMPLOYEE")
                
                // Allow users to perform CRUD data operations on customers, employees, hotels, and rooms
                .requestMatchers("/api/management/customers").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/customers/**").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/employees").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/employees/**").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/hotels").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/hotels/**").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/rooms").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/rooms/**").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/bookings/**").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/rentings/**").hasAuthority("ROLE_EMPLOYEE")
                .anyRequest().authenticated()
            );
            
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
