package contacts.security;

import contacts.model.User;
import contacts.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolves the caller behind the current request.
 *
 * {@link JwtAuthFilter} puts the JWT subject — the Mongo user id — in as the authentication
 * principal, so nothing here has to re-parse the token. Every method throws 401 rather than
 * returning null: these are only ever called from endpoints the security chain has already
 * marked as authenticated, so an absent principal is a wiring bug, not a normal state.
 */
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String requireId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String id) || id.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return id;
    }

    /** Null when unauthenticated, for endpoints that work with or without a caller. */
    public String idOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String id) || id.isBlank()) {
            return null;
        }
        return id;
    }

    public User require() {
        String id = requireId();
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }
}
