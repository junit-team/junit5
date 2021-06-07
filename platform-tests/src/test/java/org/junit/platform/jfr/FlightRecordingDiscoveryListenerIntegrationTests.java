/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.jfr;

import static dev.morling.jfrunit.ExpectedEvent.event;
import static dev.morling.jfrunit.JfrEventsAssert.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import dev.morling.jfrunit.EnableEvent;
import dev.morling.jfrunit.JfrEventTest;
import dev.morling.jfrunit.JfrEvents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly;

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
