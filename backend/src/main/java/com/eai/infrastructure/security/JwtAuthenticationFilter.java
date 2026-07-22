package com.eai.infrastructure.security;

import com.eai.application.common.UnauthorizedException;
import com.eai.application.security.AuthenticatedUser;
import com.eai.application.security.TokenProvider;
import com.eai.application.user.UserRepository;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            AuthenticatedUser authenticatedUser;
            try {
                authenticatedUser = tokenProvider.parseAccessToken(authorization.substring(7));
                if (userRepository.findById(authenticatedUser.id()).filter(user -> user.isActive()).isEmpty()) {
                    throw new UnauthorizedException("Invalid access token");
                }
            } catch (UnauthorizedException exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                return;
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    authenticatedUser.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                            .toList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
