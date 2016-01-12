
package org.junit.gen5.engine;

import static org.junit.gen5.engine.ClassFilters.classNameMatches;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * The {@code TestPlanSpecificationBuilder} provides a light-weight DSL for
 * generating a {@link TestPlanSpecification}.
 *
 * <p>Example:
 *
 * <pre>
 *   testPlan()
 *     .packages()
 *       .name("org.junit.gen5")
 *       .name("com.junit.samples")
 *       .and()
 *     .classes()
 *       .testClass(TestDescriptorTests.class)
 *       .name("com.junit.samples.SampleTestCase")
 *       .and()
 *     .methods()
 *       .method(TestDescriptorTests.class, "test1")
 *       .method("com.junit.samples.SampleTestCase", "test2")
 *       .and()
 *     .filters()
 *       .engines()
 *         .includeId("junit5")
 *       .classNames()
 *         .pattern("org.junit.gen5.tests")
 *         .pattern("org.junit.sample")
 *       .tags()
 *         .include("Fast")
 *         .exclude("Slow")
 *     .build();
 * </pre>
 */
public class TestPlanSpecificationBuilder {
	private List<String> packages = new LinkedList<>();
	private List<String> classNames = new LinkedList<>();
	private List<Class<?>> classes = new LinkedList<>();
	private List<MethodConfig> methods = new LinkedList<>();

	private List<String> includeTags = new LinkedList<>();
	private List<String> excludeTags = new LinkedList<>();
	private List<String> engineIds = new LinkedList<>();
	private List<String> classNamePatterns = new LinkedList<>();

	public static TestPlanSpecificationBuilder testPlan() {
		return new TestPlanSpecificationBuilder();
	}

	private TestPlanSpecificationBuilder() {
		//
	}

	public TestPlanSpecification build() {
		List<TestPlanSpecificationElement> elements = new LinkedList<>();

		packages.forEach((testPackage) -> elements.add(TestPlanSpecification.forPackage(testPackage)));
		classes.forEach((testClass) -> elements.add(TestPlanSpecification.forClass(testClass)));
		classNames.forEach((className) -> elements.add(TestPlanSpecification.forName(className)));
		methods.forEach(
			(method) -> elements.add(TestPlanSpecification.forMethod(method.getTestClass(), method.getTestMethod())));

		TestPlanSpecification testPlanSpecification = TestPlanSpecification.build(elements);

		classNamePatterns.forEach((pattern) -> testPlanSpecification.filterWith(classNameMatches(pattern)));
		testPlanSpecification.filterWith(TestPlanSpecification.byTags(includeTags));
		testPlanSpecification.filterWith(TestPlanSpecification.excludeTags(excludeTags));
		engineIds.forEach((engineId) -> testPlanSpecification.filterWith(TestPlanSpecification.byEngine(engineId)));

		return testPlanSpecification;
	}

	public PackageConfiguration packages() {
		return new PackageConfiguration();
	}

	public ClassConfiguration classes() {
		return new ClassConfiguration();
	}

	public MethodConfiguration methods() {
		return new MethodConfiguration();
	}

	public FilterConfiguration filters() {
		return new FilterConfiguration();
	}

	public abstract class Configuration {
		public TestPlanSpecificationBuilder and() {
			return TestPlanSpecificationBuilder.this;
		}

		public TestPlanSpecification build() {
			return TestPlanSpecificationBuilder.this.build();
		}
	}

	public class PackageConfiguration extends Configuration {
		public PackageConfiguration name(String packageNames) {
			TestPlanSpecificationBuilder.this.packages.add(packageNames);
			return this;
		}
	}

	public class ClassConfiguration extends Configuration {
		public ClassConfiguration name(String className) {
			TestPlanSpecificationBuilder.this.classNames.add(className);
			return this;
		}

		public ClassConfiguration testClass(Class<?> testClass) {
			TestPlanSpecificationBuilder.this.classes.add(testClass);
			return this;
		}
	}

	public class MethodConfiguration extends Configuration {
		public MethodConfiguration method(Class<?> testClass, String methodName) {
			TestPlanSpecificationBuilder.this.methods.add(new MethodConfig(testClass.getName(), methodName));
			return this;
		}

		public MethodConfiguration method(String className, String methodName) {
			TestPlanSpecificationBuilder.this.methods.add(new MethodConfig(className, methodName));
			return this;
		}
	}

	public static class MethodConfig {
		private final String testClassName;
		private final String testMethodName;

		public MethodConfig(String testClassName, String testMethodName) {
			this.testClassName = testClassName;
			this.testMethodName = testMethodName;
		}

		public Class<?> getTestClass() {
			return ReflectionUtils.loadClass(testClassName).get();
		}

		public Method getTestMethod() {
			return ReflectionUtils.findMethod(getTestClass(), testMethodName).get();
		}
	}

	public class FilterConfiguration extends Configuration {
		public TagFilterConfiguration tags() {
			return new TagFilterConfiguration();
		}

		public EngineFilterConfiguration engines() {
			return new EngineFilterConfiguration();
		}

		public ClassNameFilterConfiguration classNames() {
			return new ClassNameFilterConfiguration();
		}
	}

	public class TagFilterConfiguration extends FilterConfiguration {
		public TagFilterConfiguration exclude(String tag) {
			TestPlanSpecificationBuilder.this.excludeTags.add(tag);
			return this;
		}

		public TagFilterConfiguration include(String tag) {
			TestPlanSpecificationBuilder.this.includeTags.add(tag);
			return this;
		}
	}

	public class EngineFilterConfiguration extends FilterConfiguration {
		public EngineFilterConfiguration includeId(String engineId) {
			TestPlanSpecificationBuilder.this.engineIds.add(engineId);
			return this;
		}
	}

	public class ClassNameFilterConfiguration extends FilterConfiguration {
		public ClassNameFilterConfiguration pattern(String pattern) {
			TestPlanSpecificationBuilder.this.classNamePatterns.add(pattern);
			return this;
		}
	}
}
