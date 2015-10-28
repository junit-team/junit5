
package org.junit.gen5.launcher;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

/**
 * @since 5.0
 */
final class TestPlan {

	private static enum State {
		NEW, ACTIVE, PAUSED, STOPPED, COMPLETED;
	}


	/**
	 * List of all TestDescriptors, including children.
	 */
	private final Collection<TestDescriptor> testDescriptors = new LinkedList<>();

	private State state = State.NEW;


	TestPlan() {
		/* no-op */
	}

	public boolean isActive() {
		return this.state == State.ACTIVE;
	}

	public boolean isPaused() {
		return this.state == State.PAUSED;
	}

	public boolean isStopped() {
		return this.state == State.STOPPED;
	}

	public boolean isCompleted() {
		return this.state == State.COMPLETED;
	}

	public void addTest(TestDescriptor testDescriptor) {
		addTests(singleton(testDescriptor));
	}

	public void addTests(TestDescriptor... testDescriptors) {
		addTests(asList(testDescriptors));
	}

	public void addTests(Collection<TestDescriptor> testDescriptors) {
		this.testDescriptors.addAll(testDescriptors);
	}

	public Collection<TestDescriptor> getTests() {
		return Collections.unmodifiableCollection(testDescriptors);
	}

	public List<TestDescriptor> getAllTestsForTestEngine(TestEngine testEngine) {
		return this.testDescriptors.stream().filter(testEngine::supports).collect(Collectors.toList());
	}

	public void start() {
		System.out.println("Starting test plan");
		this.state = State.ACTIVE;
	}

	public void stop() {
		System.out.println("Stopping test plan");
		this.state = State.STOPPED;
	}

	public void pause() {
		System.out.println("Pausing test plan");
		this.state = State.PAUSED;
	}

	public void restart() {
		System.out.println("Restarting test plan");
		this.state = State.ACTIVE;
	}

}
