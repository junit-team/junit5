
package org.junit.launch;

import java.nio.file.Path;
import java.util.Map;

import org.junit.core.TestDescriptor;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public final class TestPlan {

	private static enum State {
		ACTIVE, PAUSED, STOPPED, COMPLETED
	};


	private State state;


	private TestPlan() {
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

	/**
	 * Get the {@link TestDescriptor} that serves as the root of the test
	 * plan tree.
	 */
	public TestDescriptor getTestTree() {
		return null;
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

	public static TestPlanBuilder builder() {
		return new TestPlanBuilder();
	}

	public static final class TestPlanBuilder {

		private TestPlanBuilder() {
			/* no-op */
		}

		public TestPlanBuilder configuration(Map<String, String> parameters) {
			return this;
		}

		public TestPlanBuilder classes(Class<?>... classes) {
			return this;
		}

		public TestPlanBuilder classNames(String... classNames) {
			return this;
		}

		public TestPlanBuilder packages(Package... packages) {
			return this;
		}

		public TestPlanBuilder packageNames(String... packageNames) {
			return this;
		}

		public TestPlanBuilder paths(Path... paths) {
			return this;
		}

		public TestPlanBuilder fileNames(String... fileNames) {
			return this;
		}

		public TestPlanBuilder descriptorIds(String... descriptorIds) {
			return this;
		}

		public TestPlanBuilder includePatterns(String... patterns) {
			return this;
		}

		public TestPlanBuilder excludePatterns(String... patterns) {
			return this;
		}

		public TestPlanBuilder listeners(TestPlanListener... listeners) {
			return this;
		}

		public TestPlan build() {
			return new TestPlan();
		}

	}

}
