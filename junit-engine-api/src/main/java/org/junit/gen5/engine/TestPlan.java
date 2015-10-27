
package org.junit.gen5.engine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
// TODO make immutable
@Data
public final class TestPlan {

	private Map<String, String> launchParameters = new HashMap<>();
	private List<Class<?>> classes = new LinkedList<>();
	private List<String> classNames = new LinkedList<>();
	private List<Package> packages = new LinkedList<>();
	private List<String> packageNames = new LinkedList<>();
	private List<Path> paths = new LinkedList<>();
	private List<String> fileNames = new LinkedList<>();
	private List<String> includePatterns = new LinkedList<>();
	private List<String> excludePatterns = new LinkedList<>();

	private TestPlan() {
		/* no-op */
	}

	public static TestPlanBuilder builder() {
		return new TestPlanBuilder();
	}

	public static final class TestPlanBuilder {

		private TestPlan testPlan = new TestPlan();

		private TestPlanBuilder() {
			/* no-op */
		}

		public TestPlanBuilder parameter(String key, String value) {
			testPlan.getLaunchParameters().put(key, value);
			return this;
		}

		public TestPlanBuilder parameters(Map<String, String> launchParameters) {
			testPlan.getLaunchParameters().putAll(launchParameters);
			return this;
		}

		public TestPlanBuilder classes(Class<?>... classes) {
			Arrays.stream(classes).forEach(c -> testPlan.getClasses().add(c));
			return this;
		}

		public TestPlanBuilder classNames(String... classNames) {
			Arrays.stream(classNames).forEach(n -> testPlan.getClassNames().add(n));
			return this;
		}

		public TestPlanBuilder packages(Package... packages) {
			Arrays.stream(packages).forEach(p -> testPlan.getPackages().add(p));
			return this;
		}

		public TestPlanBuilder packageNames(String... packageNames) {
			Arrays.stream(packageNames).forEach(n -> testPlan.getPackageNames().add(n));
			return this;
		}

		public TestPlanBuilder paths(Path... paths) {
			Arrays.stream(paths).forEach(p -> testPlan.getPaths().add(p));
			return this;
		}

		public TestPlanBuilder fileNames(String... fileNames) {
			Arrays.stream(fileNames).forEach(n -> testPlan.getFileNames().add(n));
			return this;
		}

		public TestPlanBuilder include(String... patterns) {
			Arrays.stream(patterns).forEach(p -> testPlan.getIncludePatterns().add(p));
			return this;
		}

		public TestPlanBuilder exclude(String... patterns) {
			Arrays.stream(patterns).forEach(p -> testPlan.getExcludePatterns().add(p));
			return this;
		}

		public TestPlan build() {
			return testPlan;
		}
	}

}
