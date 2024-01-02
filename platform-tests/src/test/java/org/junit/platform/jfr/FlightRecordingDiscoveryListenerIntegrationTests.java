/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.jfr;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.moditect.jfrunit.ExpectedEvent.event;
import static org.moditect.jfrunit.JfrEventsAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly;
import org.moditect.jfrunit.EnableEvent;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;

@JfrEventTest
public class FlightRecordingDiscoveryListenerIntegrationTests {

	public JfrEvents jfrEvents = new JfrEvents();

	@Test
	@EnableEvent("org.junit.*")
	void reportsEvents() {
		var launcher = LauncherFactoryForTestingPurposesOnly.createLauncher(new JupiterTestEngine());
		var request = request() //
				.selectors(selectClass(FlightRecordingDiscoveryListenerIntegrationTests.class)) //
				.listeners(new FlightRecordingDiscoveryListener()) //
				.build();

		launcher.discover(request);
		jfrEvents.awaitEvents();

		assertThat(jfrEvents) //
				.contains(event("org.junit.LauncherDiscovery") //
				// TODO JfrUnit does not yey support checking int values
				//						.with("selectors", 1) //
				//						.with("filters", 0) //
				) //
				.contains(event("org.junit.EngineDiscovery") //
						.with("uniqueId", "[engine:junit-jupiter]"));
	}
}
