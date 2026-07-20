package com.samaanlink;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Standing CI gate for the module-boundary rules in the architecture doc: no module reaching into
 * another module's repository/domain/infrastructure package, no circular module dependencies, only
 * a module's {@code application} named interface is visible to other modules.
 *
 * <p>Only {@code com.samaanlink.*} subpackages are analyzed - the legacy {@code com.foodyexpress}
 * package is a sibling, not a descendant, of {@code com.samaanlink}, so it is intentionally outside
 * this test's scope until it is fully replaced (Task 6).
 */
class ModularityTests {

	private final ApplicationModules modules = ApplicationModules.of(SamaanLinkApplication.class);

	@Test
	void verifiesModularStructure() {
		modules.verify();
	}
}
