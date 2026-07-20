package com.samaanlink.identity.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.identity.domain.Role;
import com.samaanlink.identity.domain.User;

/**
 * Bootstraps a single SUPER_ADMIN account on first startup so the Identity module's JWT login can
 * be exercised end-to-end before any real onboarding flow exists. Idempotent (no-ops once any user
 * exists). This is a development convenience, not a production seeding strategy - revisit under
 * Phase 7 hardening before a real deployment.
 */
@Component
public class DevAdminSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DevAdminSeeder.class);
	private static final String DEFAULT_EMAIL = "admin@samaanlink.dev";
	private static final String DEFAULT_PASSWORD = "ChangeMe123!";

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public DevAdminSeeder(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (userRepository.count() > 0) {
			return;
		}
		Role superAdmin = roleRepository.findByName("SUPER_ADMIN")
				.orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role missing - check Flyway seed migration"));

		userRepository.save(User.create(DEFAULT_EMAIL, passwordEncoder.encode(DEFAULT_PASSWORD), "Super", "Admin",
				null, superAdmin));
		log.warn("Seeded default SUPER_ADMIN account {} with a known dev password - change it before any shared use",
				DEFAULT_EMAIL);
	}
}
