package com.yow.access.config.security.context;

import com.yow.access.entities.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Authenticated user context that extracts user ID from the SecurityContext.
 * The JwtAuthenticationFilter sets the AppUser as the principal.
 */
@Component
public class MockAuthenticatedUserContext implements AuthenticatedUserContext {

    @Override
    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AppUser appUser) {
            return appUser.getId();
        }

        throw new IllegalStateException("Principal is not an AppUser: " + principal.getClass().getName());
    }
}
