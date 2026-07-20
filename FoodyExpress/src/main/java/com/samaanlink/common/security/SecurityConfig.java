package com.samaanlink.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Bridge security configuration for the migration window.
 *
 * <p>Adding {@code spring-boot-starter-security} to the classpath turns on Spring Security's
 * default auto-configuration, which would otherwise put every endpoint (including the still-live
 * legacy FoodyExpress controllers) behind a generated-password login. Until the Identity module
 * (Task 3) introduces real JWT authentication and the role/permission matrix from the architecture
 * doc, this permits all requests through.
 *
 * <p>CSRF is disabled explicitly rather than left to defaults: Spring Security 7 (Spring Boot 4)
 * enforces CSRF protection on state-changing requests by default, which would 403 every POST/PUT
 * /DELETE on this API. SamaanLink's APIs are stateless (JWT bearer tokens, no browser session
 * cookies), so CSRF protection — designed for cookie-based session auth — does not apply; this
 * stays disabled once the Identity module lands too, not just during the bridge period.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}
}
