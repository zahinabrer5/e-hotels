package org.uncreatives.e_hotels.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.uncreatives.e_hotels.config.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    public AuthController(JwtUtil jwtUtil, JdbcTemplate jdbcTemplate) {
        this.jwtUtil = jwtUtil;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    @Operation(summary = "Login to get JWT Token", description = "Enter your ID as username, and ROLE_EMPLOYEE or ROLE_CUSTOMER as role.")
    public String login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Employee Login", value = "{\n  \"username\": \"111-000-001\",\n  \"role\": \"ROLE_EMPLOYEE\"\n}"),
                    @ExampleObject(name = "Customer Login", value = "{\n  \"username\": \"222-000-001\",\n  \"role\": \"ROLE_CUSTOMER\"\n}")
            }))
            @RequestBody Map<String, String> credentials) {
        String role = credentials.get("role"); // this can be "ROLE_CUSTOMER" or "ROLE_EMPLOYEE"
        String username = credentials.get("username"); // ID for Employee or Customer
        
        if (role == null || username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing role or username");
        }

        // Validate the user exists in the database
        Integer count = 0;
        if ("ROLE_EMPLOYEE".equals(role)) {
            count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Employee WHERE National_ID = ?", Integer.class, username);
        } else if ("ROLE_CUSTOMER".equals(role)) {
            count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Customer WHERE custID = ?", Integer.class, username);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role specified!");
        }

        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid ID. User not found in database!");
        }

        return jwtUtil.generateToken(username, role);
    }
}
