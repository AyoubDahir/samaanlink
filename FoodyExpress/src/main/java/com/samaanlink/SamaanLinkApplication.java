package com.samaanlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Entry point for the SamaanLink modular monolith.
 *
 * <p>During the migration from the legacy {@code com.foodyexpress} codebase, this class
 * explicitly scans both {@code com.samaanlink} (the target modular structure) and
 * {@code com.foodyexpress} (the legacy code being incrementally replaced module by module).
 * {@code com.foodyexpress} is <strong>not</strong> a subpackage of {@code com.samaanlink} and is
 * therefore invisible to Spring Modulith's module-boundary verification — it is intentionally
 * exempt until it is fully replaced and deleted, at which point these bridge attributes should
 * be removed and this class should scan only {@code com.samaanlink}.
 */
@SpringBootApplication(scanBasePackages = { "com.samaanlink", "com.foodyexpress" })
@EntityScan(basePackages = { "com.samaanlink", "com.foodyexpress" })
@EnableJpaRepositories(basePackages = { "com.samaanlink", "com.foodyexpress" })
@EnableJpaAuditing
public class SamaanLinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(SamaanLinkApplication.class, args);
	}

}
