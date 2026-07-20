package com.samaanlink.common.audit;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

/**
 * Placeholder {@link AuditorAware} so {@code @EnableJpaAuditing}'s {@code @CreatedBy}/
 * {@code @LastModifiedBy} support has a bean to call before the Identity module exists. Returns
 * empty (no actor recorded) until the Identity module's authentication filter populates the
 * security context, at which point this should be replaced with a bean that reads the
 * authenticated user's id from {@code SecurityContextHolder}.
 */
@Configuration
public class AuditorAwareConfig {

	@Bean
	public AuditorAware<UUID> auditorAware() {
		return Optional::empty;
	}
}
