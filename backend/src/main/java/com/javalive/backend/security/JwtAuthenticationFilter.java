package com.javalive.backend.security;

import com.javalive.backend.repository.AdminRepository;
import com.javalive.backend.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads a `Bearer` JWT, resolves it against either the user or admin guard depending on the
 * token's "role" claim (issued by AuthService / AdminAuthService respectively), and populates the
 * SecurityContext. Two guards share one filter since both ride the same Authorization header.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository, AdminRepository adminRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            if (jwtService.isValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Long subjectId = jwtService.extractSubjectId(token);
                String role = jwtService.extractRole(token);

                Object principal = null;
                if ("ADMIN".equals(role)) {
                    principal = adminRepository.findById(subjectId).map(AdminPrincipal::new).orElse(null);
                } else if ("USER".equals(role)) {
                    principal = userRepository.findById(subjectId).map(UserPrincipal::new).orElse(null);
                }

                if (principal instanceof org.springframework.security.core.userdetails.UserDetails details
                        && details.isEnabled()) {
                    var authToken = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Invalid/expired token — leave the SecurityContext empty so downstream authorization
            // rejects the request normally (401/403), rather than surfacing a parse error to the client.
        }

        filterChain.doFilter(request, response);
    }
}
