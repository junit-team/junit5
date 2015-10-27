
package org.junit.gen5.launcher;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public final class TestPlan {

	private TestPlan() {
		/* no-op */
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

		public TestPlan build() {
			return new TestPlan();
		}

	}

}
