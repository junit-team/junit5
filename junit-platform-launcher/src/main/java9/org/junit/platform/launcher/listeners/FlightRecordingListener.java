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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestExecutionResult;
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

	private final AtomicReference<TestPlanExecutionEvent> testPlanExecutionEvent;
	private final Map<String, TestExecutionEvent> testExecutionEventMap;

	public FlightRecordingListener() {
		this.testPlanExecutionEvent = new AtomicReference<>();
		this.testExecutionEventMap = new ConcurrentHashMap<>();
	}

	@Override
	public void testPlanExecutionStarted(TestPlan plan) {
		TestPlanExecutionEvent event = new TestPlanExecutionEvent();
		event.containsTests = plan.containsTests();
		event.rootContainerIds = plan.getRoots().stream().map(TestIdentifier::getUniqueId).collect(
			Collectors.joining("#"));
		testPlanExecutionEvent.set(event);
		event.begin();
	}

	@Override
	public void testPlanExecutionFinished(TestPlan plan) {
		TestPlanExecutionEvent event = testPlanExecutionEvent.get();
		event.commit();
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier test) {
		DynamicTestEvent event = new DynamicTestEvent();
		event.uniqueId = test.getUniqueId();
		event.displayName = test.getDisplayName();
		event.commit();
	}

	@Override
	public void executionSkipped(TestIdentifier test, String reason) {
		SkippedTestEvent event = new SkippedTestEvent();
		event.uniqueId = test.getUniqueId();
		event.displayName = test.getDisplayName();
		event.reason = reason;
		event.commit();
	}

	@Override
	public void executionStarted(TestIdentifier test) {
		TestExecutionEvent event = new TestExecutionEvent();
		event.uniqueId = test.getUniqueId();
		event.displayName = test.getDisplayName();
		event.begin();
		testExecutionEventMap.put(test.getUniqueId(), event);
	}

	@Override
	public void executionFinished(TestIdentifier test, TestExecutionResult result) {
		TestExecutionEvent event = testExecutionEventMap.get(test.getUniqueId());
		event.end();
		event.result = result.getStatus().toString();
		event.commit();
	}

	@Category("JUnit")
	@Name("org.junit.TestPlan")
	@Label("Test Plan")
	static class TestPlanExecutionEvent extends Event {

		@Label("Contains Tests")
		boolean containsTests;

		@Label("Root Containers Ids")
		String rootContainerIds;
	}

	@Category("JUnit")
	@Name("org.junit.Test")
	@Label("Test")
	static abstract class TestEvent extends Event {

		@Label("Unique Id")
		String uniqueId;

		@Label("Display Name")
		String displayName;
	}

	@Name("org.junit.DynamicTestRegistration")
	static class DynamicTestEvent extends TestEvent {
	}

	@Name("org.junit.SkippedTest")
	static class SkippedTestEvent extends TestEvent {
		@Label("Reason")
		String reason;
	}

	@Name("org.junit.TestExecution")
	static class TestExecutionEvent extends TestEvent {
		@Label("Result")
		String result;
	}
}
