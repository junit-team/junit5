/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherFactory;

@TestMethodOrder(Alphanumeric.class)
class JupiterIntegrationTests {

	@Test
	void packageName() {
		assertEquals("integration", getClass().getPackageName());
	}

	@Test
	void moduleIsNamed() {
		assumeTrue(getClass().getModule().isNamed(), "not running on the module-path");
		assertEquals("integration", getClass().getModule().getName());
	}

	@Test
	void resolve() {
		var selector = DiscoverySelectors.selectClass(getClass());
		var testPlan = LauncherFactory.create().discover(
			request().selectors(selector).filters(includeEngines("junit-jupiter")).build());

		var engine = testPlan.getRoots().iterator().next();

		assertEquals(1, testPlan.getChildren(engine).size()); // JupiterIntegrationTests.class
		assertEquals(3, testPlan.getChildren(testPlan.getChildren(engine).iterator().next()).size()); // 3 test methods
	}

}
