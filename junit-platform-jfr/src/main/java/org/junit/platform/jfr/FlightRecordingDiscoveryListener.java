/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.jfr;

import static java.util.Objects.requireNonNull;
import static org.apiguardian.api.API.Status.STABLE;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.launcher.EngineDiscoveryResult;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * A {@link LauncherDiscoveryListener} that generates Java Flight Recorder
 * events.
 *
 * @since 1.8
 * @see <a href="https://openjdk.java.net/jeps/328">JEP 328: Flight Recorder</a>
 */
@API(status = STABLE, since = "1.11")
public class FlightRecordingDiscoveryListener implements LauncherDiscoveryListener {

	private final AtomicReference<@Nullable LauncherDiscoveryEvent> launcherDiscoveryEvent = new AtomicReference<>();
	private final Map<org.junit.platform.engine.UniqueId, EngineDiscoveryEvent> engineDiscoveryEvents = new ConcurrentHashMap<>();

	@Override
	public void launcherDiscoveryStarted(LauncherDiscoveryRequest request) {
		LauncherDiscoveryEvent event = new LauncherDiscoveryEvent();
		event.selectors = request.getSelectorsByType(DiscoverySelector.class).size();
		event.filters = request.getFiltersByType(DiscoveryFilter.class).size();
		event.begin();
		launcherDiscoveryEvent.set(event);
	}

	@Override
	public void launcherDiscoveryFinished(LauncherDiscoveryRequest request) {
		requireNonNull(launcherDiscoveryEvent.getAndSet(null)).commit();
	}

	@Override
	public void engineDiscoveryStarted(org.junit.platform.engine.UniqueId engineId) {
		EngineDiscoveryEvent event = new EngineDiscoveryEvent();
		event.uniqueId = engineId.toString();
		event.begin();
		engineDiscoveryEvents.put(engineId, event);
	}

	@Override
	public void engineDiscoveryFinished(org.junit.platform.engine.UniqueId engineId, EngineDiscoveryResult result) {
		EngineDiscoveryEvent event = engineDiscoveryEvents.remove(engineId);
		event.result = result.getStatus().toString();
		event.commit();
	}

	@Override
	public void issueEncountered(org.junit.platform.engine.UniqueId engineId, DiscoveryIssue issue) {
		DiscoveryIssueEvent event = new DiscoveryIssueEvent();
		event.engineId = engineId.toString();
		event.severity = issue.severity().name();
		event.message = issue.message();
		event.source = issue.source().map(Object::toString).orElse(null);
		event.cause = issue.cause().map(ExceptionUtils::readStackTrace).orElse(null);
		event.commit();
	}

	@Category({ "JUnit", "Discovery" })
	@StackTrace(false)
	abstract static class DiscoveryEvent extends Event {
	}

	@Label("Test Discovery")
	@Name("org.junit.LauncherDiscovery")
	static class LauncherDiscoveryEvent extends DiscoveryEvent {

		@Label("Number of selectors")
		int selectors;

		@Label("Number of filters")
		int filters;
	}

	@Label("Engine Discovery")
	@Name("org.junit.EngineDiscovery")
	static class EngineDiscoveryEvent extends DiscoveryEvent {

		@UniqueId
		@Label("Unique Id")
		@Nullable
		String uniqueId;

		@Label("Result")
		@Nullable
		String result;
	}

	@Label("Discovery Issue")
	@Name("org.junit.DiscoveryIssue")
	static class DiscoveryIssueEvent extends DiscoveryEvent {

		@Label("Engine Id")
		@Nullable
		String engineId;

		@Label("Severity")
		@Nullable
		String severity;

		@Label("Message")
		@Nullable
		String message;

		@Label("Source")
		@Nullable
		String source;

		@Label("Cause")
		@Nullable
		String cause;
	}
}
