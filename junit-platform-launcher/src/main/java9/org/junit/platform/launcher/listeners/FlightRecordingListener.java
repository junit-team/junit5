/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.MetadataDefinition;
import jdk.jfr.Name;
import jdk.jfr.Relational;
import jdk.jfr.StackTrace;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * A {@link TestExecutionListener} that generates Java Flight Recorder
 * events.
 *
 * @see <a href="https://openjdk.java.net/jeps/328">JEP 328: Flight Recorder</a>
 * @since 1.7
 */
@API(status = EXPERIMENTAL, since = "1.7")
public class FlightRecordingListener implements TestExecutionListener {

	private final AtomicReference<TestPlanExecutionEvent> testPlanExecutionEvent = new AtomicReference<>();
	private final Map<String, TestExecutionEvent> testExecutionEventMap = new ConcurrentHashMap<>();

	@Override
	public void testPlanExecutionStarted(TestPlan plan) {
		TestPlanExecutionEvent event = new TestPlanExecutionEvent();
		event.containsTests = plan.containsTests();
		event.engineNames = plan.getRoots().stream().map(TestIdentifier::getDisplayName).collect(
			Collectors.joining(", "));
		testPlanExecutionEvent.set(event);
		event.begin();
	}

	@Override
	public void testPlanExecutionFinished(TestPlan plan) {
		TestPlanExecutionEvent event = testPlanExecutionEvent.get();
		event.commit();
	}

	@Override
	public void executionSkipped(TestIdentifier test, String reason) {
		SkippedTestEvent event = new SkippedTestEvent();
		event.initialize(test);
		event.reason = reason;
		event.commit();
	}

	@Override
	public void executionStarted(TestIdentifier test) {
		TestExecutionEvent event = new TestExecutionEvent();
		testExecutionEventMap.put(test.getUniqueId(), event);
		event.initialize(test);
		event.begin();
	}

	@Override
	public void executionFinished(TestIdentifier test, TestExecutionResult result) {
		if (test.isContainer() && result.getStatus().equals(TestExecutionResult.Status.SUCCESSFUL)) {
			return;
		}
		Optional<Throwable> throwable = result.getThrowable();
		TestExecutionEvent event = testExecutionEventMap.get(test.getUniqueId()); // TODO Remove?
		event.end();
		event.result = result.getStatus().toString();
		event.exceptionClass = throwable.map(Throwable::getClass).orElse(null);
		event.exceptionMessage = throwable.map(Throwable::getMessage).orElse(null);
		event.commit();
	}

	@Override
	public void reportingEntryPublished(TestIdentifier test, ReportEntry reportEntry) {
		for (Map.Entry<String, String> entry : reportEntry.getKeyValuePairs().entrySet()) {
			ReportEntryEvent event = new ReportEntryEvent();
			event.uniqueId = test.getUniqueId();
			event.key = entry.getKey();
			event.value = entry.getValue();
			event.commit();
		}
	}

	@Category("JUnit")
	@Label("Test Plan")
	@Name("org.junit.TestPlan")
	@StackTrace(false)
	static class TestPlanExecutionEvent extends Event {
		@Label("Contains Tests")
		boolean containsTests;
		@Label("Engine Names")
		String engineNames;
	}

	@MetadataDefinition
	@Relational
	@Name("org.junit.UniqueId")
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface UniqueId {
	}

	@Category("JUnit")
	@StackTrace(false)
	abstract static class TestEvent extends Event {
		@UniqueId
		@Label("Unique Id")
		String uniqueId;
		@Label("Display Name")
		String displayName;
		@Label("Tags")
		String tags;
		@Label("Type")
		String type;

		void initialize(TestIdentifier test) {
			this.uniqueId = test.getUniqueId();
			this.displayName = test.getDisplayName();
			this.tags = test.getTags().isEmpty() ? null : test.getTags().toString();
			this.type = test.getType().name();
		}
	}

	@Label("Skipped Test")
	@Name("org.junit.SkippedTest")
	static class SkippedTestEvent extends TestEvent {
		@Label("Reason")
		String reason;
	}

	@Label("Test")
	@Name("org.junit.TestExecution")
	static class TestExecutionEvent extends TestEvent {
		@Label("Result")
		String result;
		@Label("Exception Class")
		Class<?> exceptionClass;
		@Label("Exception Message")
		String exceptionMessage;
	}

	@Category("JUnit")
	@Label("Report Entry")
	@Name("org.junit.ReportEntry")
	@StackTrace(false)
	static class ReportEntryEvent extends Event {
		@UniqueId
		@Label("Unique Id")
		String uniqueId;
		@Label("Key")
		String key;
		@Label("Value")
		String value;
	}
}
