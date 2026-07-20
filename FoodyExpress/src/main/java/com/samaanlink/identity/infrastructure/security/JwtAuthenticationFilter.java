package com.samaanlink.identity.infrastructure.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Stateless bearer-token authentication: validates the JWT signature/expiry and builds the
 * {@link org.springframework.security.core.Authentication} directly from its claims (role and
 * permission codes) - no database lookup per request. Requests without a valid token simply pass
 * through unauthenticated; {@code SecurityConfig}'s authorization rules decide what that means for
 * a given endpoint.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			try {
				Claims claims = jwtTokenProvider.parseClaims(header.substring(7));
				UUID userId = jwtTokenProvider.subject(claims);
				String role = claims.get("role", String.class);
				@SuppressWarnings("unchecked")
				List<String> permissions = claims.get("permissions", List.class);

				List<GrantedAuthority> authorities = new java.util.ArrayList<>();
				if (role != null) {
					authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
				}
				if (permissions != null) {
					permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
				}

				var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | IllegalArgumentException ex) {
				SecurityContextHolder.clearContext();
			}
		}
		chain.doFilter(request, response);
	}
}
