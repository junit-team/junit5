/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.execution;

import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_DYNAMIC;

import java.util.Optional;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;
import org.junit.vintage.engine.support.UniqueIdReader;
import org.junit.vintage.engine.support.UniqueIdStringifier;

/**
 * @since 4.12
 */
class RunListenerAdapter extends RunListener {
	enum State {
		NOT_STARTED, IGNORED, STARTED, FAILED
	}

	private static class Frame {
		private final Frame prev;
		private final TestDescriptor testDescriptor;
		private State state = State.NOT_STARTED;

		private Frame(Frame prev, TestDescriptor testDescriptor) {
			this.prev = prev;
			this.testDescriptor = testDescriptor;
		}

		@Override
		public String toString() {
			if (prev == null) {
				return "RootFrame";
			}
			return "Frame,prev:" + Integer.toHexString(prev.hashCode()) + "," + state + "," + testDescriptor.toString();
		}
	}

	private final InheritableThreadLocal<Frame> testFrames = new InheritableThreadLocal<Frame>() {
		@Override
		protected Frame childValue(Frame parentValue) {
			throw new UnsupportedOperationException("Fork is not implemented yet: " + parentValue);
		}
	};

	private final TestRun testRun;
	private final EngineExecutionListener listener;
	private final Function<Description, String> uniqueIdExtractor;

	RunListenerAdapter(TestRun testRun, EngineExecutionListener listener) {
		this.testRun = testRun;
		this.listener = listener;
		this.uniqueIdExtractor = new UniqueIdReader().andThen(new UniqueIdStringifier());
	}

	private void addFrame(TestDescriptor testDescriptor) {
		Frame currentFrame = testFrames.get();
		if (currentFrame.testDescriptor == testDescriptor) {
			return;
		}
		// Add missing parents
		Optional<TestDescriptor> parent = testDescriptor.getParent();
		if (parent.isPresent() && parent.get() != currentFrame.testDescriptor) {
			addFrame(parent.get());
		}
		// Note: the frame is changed by the recursive call above, so we need to call testFrames.get() again
		testFrames.set(new Frame(testFrames.get(), testDescriptor));
	}

	private VintageTestDescriptor enter(Description description) {
		Frame parentFrame = testFrames.get();
		if (parentFrame == null) {
			throw new IllegalStateException("empty suiteStack. Parent suite is not found");
		}
		VintageTestDescriptor child = testRun.lookupTestDescriptor(description);
		if (child == null) {
			TestDescriptor parent = parentFrame.testDescriptor;
			UniqueId id = parent.getUniqueId().append(SEGMENT_TYPE_DYNAMIC, uniqueIdExtractor.apply(description));
			child = new VintageTestDescriptor(id, description);
			parent.addChild(child);
			fireStarted(parentFrame); // e.g. start root frame
			testRun.dynamicTestRegistered(child);
			listener.dynamicTestRegistered(child);
		}
		addFrame(child);
		return child;
	}

	private boolean eq(Description description, TestDescriptor testDescriptor) {
		return testDescriptor instanceof VintageTestDescriptor &&
				((VintageTestDescriptor) testDescriptor).getDescription() == description;
	}

	private TestDescriptor exit(Description description) {
		Frame currentFrame = testFrames.get();
		if (currentFrame == null) {
			throw new IllegalStateException("Test stack is empty, unable to register test exit for " + description);
		}
		TestDescriptor current = currentFrame.testDescriptor;
		if (!eq(description, current)) {
			throw new IllegalStateException("Unexpected suiteStack. Expecting " + description + " got " + current);
		}
		testFrames.set(currentFrame.prev);
		return current;
	}

	@Override
	public void testRunStarted(Description description) {
		System.out.println("testRunStarted = " + description);
		RunnerTestDescriptor rootDescriptor = testRun.getRunnerTestDescriptor();
		testFrames.set(new Frame(null, rootDescriptor));
		if (description.isSuite() && description.getAnnotation(Ignore.class) != null) {
			return;
		}
		enter(description);
	}

	@Override
	public void testStarted(Description description) {
		System.out.println("testStarted = " + description);
		enter(description);
		fireStarted();
	}

	@Override
	public void testIgnored(Description description) {
		System.out.println("testIgnored = " + description);
		VintageTestDescriptor current = enter(description);
		// Current stack points to the "ignored" test, and we don't want to generate "started" event for it
		// However we still need to fire started events for all its parents, so ".prev" is here
		Frame currentFrame = testFrames.get();
		fireStarted(currentFrame.prev);
		currentFrame.state = State.IGNORED;
		listener.executionSkipped(current, determineReasonForIgnoredTest(description));
		exit(description);
	}

	private void fireStarted() {
		fireStarted(testFrames.get());
	}

	private void fireStarted(Frame frame) {
		if (frame == null || frame.state != State.NOT_STARTED) {
			return;
		}
		if (frame.prev != null) {
			fireStarted(frame.prev);
		}
		frame.state = State.STARTED;
		listener.executionStarted(frame.testDescriptor);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		handleFailure(failure, TestExecutionResult::aborted);
	}

	@Override
	public void testFailure(Failure failure) {
		System.out.println("testFailure = " + failure.getDescription());
		handleFailure(failure, TestExecutionResult::failed);
	}

	@Override
	public void testFinished(Description description) {
		System.out.println("testFinished = " + description);
		TestDescriptor current = exit(description);
		fireStarted();
		listener.executionFinished(current, testRun.getStoredResultOrSuccessful(current));
	}

	@Override
	public void testRunFinished(Result result) {
		System.out.println("testRunFinished = " + result);
		for (Frame frame = testFrames.get(); frame != null; frame = frame.prev) {
			TestDescriptor current = frame.testDescriptor;
			if (frame.state == State.IGNORED) {
				continue;
			}
			listener.executionFinished(current, testRun.getStoredResultOrSuccessful(current));
		}
	}

	private void handleFailure(Failure failure, Function<Throwable, TestExecutionResult> resultCreator) {
		VintageTestDescriptor current = enter(failure.getDescription());
		fireStarted();
		testFrames.get().state = State.FAILED;
		handleFailure(failure, resultCreator, current);
	}

	private void handleFailure(Failure failure, Function<Throwable, TestExecutionResult> resultCreator,
			TestDescriptor testDescriptor) {
		TestExecutionResult result = resultCreator.apply(failure.getException());
		testRun.storeResult(testDescriptor, result);
	}

	private String determineReasonForIgnoredTest(Description description) {
		Ignore ignoreAnnotation = description.getAnnotation(Ignore.class);
		return Optional.ofNullable(ignoreAnnotation).map(Ignore::value).orElse("<unknown>");
	}
}
