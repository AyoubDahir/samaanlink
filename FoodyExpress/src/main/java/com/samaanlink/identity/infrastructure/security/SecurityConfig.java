package com.samaanlink.identity.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Real authentication/authorization wiring, replacing the Task 1 permit-all bridge.
 *
 * <p>The still-live legacy {@code com.foodyexpress} controllers (used by the current
 * prime-frontend) have no JWT integration at all and are not being touched by this task - only
 * {@code /app/login} and {@code /app/logout} are replaced (see the deleted LoginLogoutController).
 * The remaining legacy CRUD endpoints stay permitted here as a deliberate, temporary bridge until
 * Tasks 4+ rebuild them as real modules with proper authorization; tightening this list is part of
 * each of those tasks, not a one-time cleanup at the end.
 *
 * <p>CSRF stays disabled: this API is stateless (JWT bearer tokens only, no cookie-based browser
 * sessions), and CSRF protection - which Spring Security 7 enables by default - only makes sense
 * for cookie-based session auth.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String[] LEGACY_PERMITTED_PATHS = {
			"/category/**", "/admin/**", "/customers/**", "/items/**", "/restaurants/**",
			"/order/**", "/order-history/**", "/bill/**", "/foodcart/**"
	};

	private static final String[] PUBLIC_PATHS = {
			"/api/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
	};

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_PATHS).permitAll()
						.requestMatchers(LEGACY_PERMITTED_PATHS).permitAll()
						.requestMatchers("/api/v1/**").authenticated()
						.anyRequest().permitAll())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
