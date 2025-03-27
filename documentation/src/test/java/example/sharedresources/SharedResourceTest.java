/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.sharedresources;

//tag::user_guide[]
import static org.assertj.core.api.Assertions.assertThat;

import example.FirstCustomEngine;
import example.SecondCustomEngine;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

/**
 * Shared resource test.
 */
class SharedResourceTest {

	@Test
	void runBothCustomEnginesTest() {
		LauncherConfig launcherConfig = LauncherConfig //
				.builder() //
				.addTestEngines(new FirstCustomEngine(), new SecondCustomEngine()) //
				.build();
		LauncherDiscoveryRequest launcherDiscoveryRequest = LauncherDiscoveryRequestBuilder.request().build();

		try (LauncherSession session = LauncherFactory.openSession(launcherConfig)) {
			session.getLauncher().execute(launcherDiscoveryRequest);

			assertThat(FirstCustomEngine.socket).isSameAs(SecondCustomEngine.socket);
		}
	}
}
//end::user_guide[]
