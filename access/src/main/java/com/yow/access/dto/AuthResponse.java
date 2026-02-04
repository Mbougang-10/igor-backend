package com.yow.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.List;

@Builder
public class AuthResponse {
    private String token;
    private UUID userId;
    private String email;
    private String username;
    private List<String> roles;
    private boolean mustChangePassword;

    public AuthResponse() {}

    public AuthResponse(String token, UUID userId, String email, String username, List<String> roles, boolean mustChangePassword) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.mustChangePassword = mustChangePassword;
    }

    public AuthResponse(String token, UUID userId, String email, String username, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.roles = roles;
    }

    // Builder-like pattern
    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String token;
        private UUID userId;
        private String email;
        private String username;
        private List<String> roles;
        private boolean mustChangePassword;

        public AuthResponseBuilder token(String token) { this.token = token; return this; }
        public AuthResponseBuilder userId(UUID userId) { this.userId = userId; return this; }
        public AuthResponseBuilder email(String email) { this.email = email; return this; }
        public AuthResponseBuilder username(String username) { this.username = username; return this; }
        public AuthResponseBuilder roles(List<String> roles) { this.roles = roles; return this; }
        public AuthResponseBuilder mustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; return this; }
        
        public AuthResponse build() {
            return new AuthResponse(token, userId, email, username, roles, mustChangePassword);
        }
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
}
