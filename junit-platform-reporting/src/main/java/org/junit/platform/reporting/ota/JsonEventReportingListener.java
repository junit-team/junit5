/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.ota;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NON_PRIVATE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class JsonEventReportingListener implements TestExecutionListener {

	private final ObjectMapper objectMapper;
	private BufferedWriter writer;
	private Path reportsDir;
	private Event currentTestPlanEvent;

	public JsonEventReportingListener(Path reportsDir) {
		this.reportsDir = reportsDir;
		objectMapper = new ObjectMapper().disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		Path jsonFile = reportsDir.resolve("events.json");
		try {
			writer = Files.newBufferedWriter(jsonFile);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		currentTestPlanEvent = createTestPlanExecutionStartedEvent();
		write(currentTestPlanEvent);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		write(createExecutionStartedEvent(testIdentifier));
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		write(createReportingEntryPublishedEvent(testIdentifier, entry));
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
		write(createExecutionFinishedEvent(testIdentifier, result));
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		write(createTestPlanExecutionFinishedEvent());
		try {
			writer.close();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		currentTestPlanEvent = null;
	}

	private Event createTestPlanExecutionStartedEvent() {
		TestPlanEvent event = new TestPlanEvent(EventType.start);
		event.id = "testrun_" + System.currentTimeMillis();
		event.tags = new HashSet<>(Arrays.asList("foo", "bar"));
		try {
			event.host = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		event.user = System.getProperty("user.name");
		return event;
	}

	private Event createExecutionStartedEvent(TestIdentifier testIdentifier) {
		ExecutionStartedEvent event = new ExecutionStartedEvent();
		event.id = testIdentifier.getUniqueId();
		event.parent = testIdentifier.getParentId().orElse(currentTestPlanEvent.id);
		event.name = testIdentifier.getDisplayName();
		event.tags = testIdentifier.getTags().stream().map(TestTag::getName).collect(toSet());
		return event;
	}

	private Event createReportingEntryPublishedEvent(TestIdentifier testIdentifier, ReportEntry entry) {
		ReportingEntryPublishedEvent event = new ReportingEntryPublishedEvent();
		event.id = testIdentifier.getUniqueId();
		event.timestamp = entry.getTimestamp().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
		event.values = entry.getKeyValuePairs();
		return event;
	}

	private Event createExecutionFinishedEvent(TestIdentifier testIdentifier, TestExecutionResult result) {
		ExecutionFinishedEvent event = new ExecutionFinishedEvent();
		event.id = testIdentifier.getUniqueId();
		event.status = result.getStatus();
		return event;
	}

	private Event createTestPlanExecutionFinishedEvent() {
		TestPlanEvent event = new TestPlanEvent(EventType.finish);
		event.id = currentTestPlanEvent.id;
		return event;
	}

	private synchronized void write(Event event) {
		try {
			objectMapper.writeValue(writer, event);
			writer.newLine();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@JsonAutoDetect(fieldVisibility = NON_PRIVATE)
	@JsonInclude(NON_EMPTY)
	static class Event {
		final EventType type;
		long timestamp = Instant.now().toEpochMilli();
		String id;
		String parent;
		Set<String> tags;

		Event(EventType type) {
			this.type = type;
		}
	}

	static class TestPlanEvent extends Event {
		String host;
		String user;

		TestPlanEvent(EventType eventType) {
			super(eventType);
		}
	}

	static class ExecutionStartedEvent extends Event {
		String name;

		ExecutionStartedEvent() {
			super(EventType.start);
		}
	}

	static class ExecutionFinishedEvent extends Event {
		TestExecutionResult.Status status;

		ExecutionFinishedEvent() {
			super(EventType.finish);
		}
	}

	static class ReportingEntryPublishedEvent extends Event {
		Map<String, String> values;

		ReportingEntryPublishedEvent() {
			super(EventType.progress);
		}
	}

	enum EventType {
		start, progress, finish
	}

}
