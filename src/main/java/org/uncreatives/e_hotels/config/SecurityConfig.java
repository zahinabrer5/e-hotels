package org.uncreatives.e_hotels.config;

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
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow public access to view the rooms, statistics, and views directly from the website UI
                .requestMatchers("/api/management/search-rooms").permitAll()
                .requestMatchers("/api/management/views/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                
                // Allow public access to Swagger UI & OpenAPI Docs
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Customer can create Booking
                // Employee can turn Booking to Renting or directly make a Renting
                .requestMatchers("/api/management/book").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_EMPLOYEE")
                .requestMatchers("/api/management/rent").hasAuthority("ROLE_EMPLOYEE")
                
                // Allow users to perform CRUD data operations on customers, employees, hotels, and rooms
                .requestMatchers("/api/management/customers/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_EMPLOYEE")
                .requestMatchers("/api/management/employees/**").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/hotels/**").hasAuthority("ROLE_EMPLOYEE")
                .requestMatchers("/api/management/rooms/**").hasAuthority("ROLE_EMPLOYEE")
                .anyRequest().authenticated()
            );
            
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
