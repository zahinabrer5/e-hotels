package org.uncreatives.e_hotels.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        String role = null;

        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            jwt = authorizationHeader.trim();
            if (jwt.regionMatches(true, 0, "Bearer ", 0, 7)) {
                jwt = jwt.substring(7).trim();
            }
            jwt = normalizeToken(jwt);

            if (jwt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            try {
                username = jwtUtil.extractUsername(jwt);
                role = jwtUtil.extractRole(jwt);
            } catch (Exception e) {
                // Invalid token
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                // Mock an authentication object putting the extracted role
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(new SimpleGrantedAuthority(role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String normalizeToken(String token) {
        if (token == null) {
            return "";
        }

        String normalized = token.trim();
        if (normalized.length() >= 2) {
            boolean isDoubleQuoted = normalized.startsWith("\"") && normalized.endsWith("\"");
            boolean isSingleQuoted = normalized.startsWith("'") && normalized.endsWith("'");
            if (isDoubleQuoted || isSingleQuoted) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
            }
        }

        return normalized;
    }
}
