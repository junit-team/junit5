/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode.READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode.READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocksFrom;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 1.3
 */
class NodeTreeWalkerIntegrationTests {

	LockManager lockManager = new LockManager();
	NodeTreeWalker nodeTreeWalker = new NodeTreeWalker(lockManager);

	static Stream<?> pullUpExclusiveChildResourcesToTestClassCases() {
		// @formatter:off
		return Stream.of(
				Named.of("locks from annotation", TestCaseWithResourceLockFromAnnotation.class),
				Named.of("locks from provider", TestCaseWithResourceLockFromProvider.class)
		);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("pullUpExclusiveChildResourcesToTestClassCases")
	void pullUpExclusiveChildResourcesToTestClass(Class<?> testCase) {
		var engineDescriptor = discover(testCase);

		var advisor = nodeTreeWalker.walk(engineDescriptor);

		var testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ), getReadWriteLock("a"), getReadWriteLock("b")));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		var testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()).isEqualTo(List.of());
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).contains(SAME_THREAD);
	}

	static Stream<?> setsForceExecutionModeForChildrenWithWriteLocksOnClassCases() {
		// @formatter:off
		return Stream.of(
				Named.of("locks from annotation", TestCaseWithResourceWriteLockOnClassFromAnnotation.class),
				Named.of("locks from provider", TestCaseWithResourceWriteLockOnClassFromProvider.class)
		);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("setsForceExecutionModeForChildrenWithWriteLocksOnClassCases")
	void setsForceExecutionModeForChildrenWithWriteLocksOnClass(Class<?> testCase) {
		var engineDescriptor = discover(testCase);

		var advisor = nodeTreeWalker.walk(engineDescriptor);

		var testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ), getReadWriteLock("a")));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		var testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()).isEqualTo(List.of());
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).contains(SAME_THREAD);
	}

	static Stream<?> doesntSetForceExecutionModeForChildrenWithReadLocksOnClassCases() {
		// @formatter:off
		return Stream.of(
				Named.of("locks from annotation", TestCaseWithResourceReadLockOnClassFromAnnotation.class),
				Named.of("locks from provider", TestCaseWithResourceReadLockOnClassFromProvider.class)
		);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("doesntSetForceExecutionModeForChildrenWithReadLocksOnClassCases")
	void doesntSetForceExecutionModeForChildrenWithReadLocksOnClass(Class<?> testCase) {
		var engineDescriptor = discover(testCase);

		var advisor = nodeTreeWalker.walk(engineDescriptor);

		var testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ), getReadLock("a")));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		var testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()).isEqualTo(List.of());
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).isEmpty();
	}

	static Stream<?> setsForceExecutionModeForChildrenWithReadLocksOnClassAndWriteLockOnTestCases() {
		// @formatter:off
		return Stream.of(
				Named.of("locks from annotation",
						TestCaseWithResourceReadLockOnClassAndWriteClockOnTestCaseFromAnnotation.class
				),
				Named.of("locks from provider",
						TestCaseWithResourceReadLockOnClassAndWriteClockOnTestCaseFromProvider.class
				)
		);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("setsForceExecutionModeForChildrenWithReadLocksOnClassAndWriteLockOnTestCases")
	void setsForceExecutionModeForChildrenWithReadLocksOnClassAndWriteLockOnTest(Class<?> testCase) {
		var engineDescriptor = discover(testCase);

		var advisor = nodeTreeWalker.walk(engineDescriptor);

		var testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ), getReadWriteLock("a")));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		var testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()).isEqualTo(List.of());
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).contains(SAME_THREAD);
	}

	static Stream<?> doesntSetForceExecutionModeForChildrenWithReadLocksOnClassAndReadLockOnTestCases() {
		// @formatter:off
		return Stream.of(
				Named.of("locks from annotation",
						TestCaseWithResourceReadLockOnClassAndReadClockOnTestCaseFromAnnotation.class
				),
				Named.of("locks from provider",
						TestCaseWithResourceReadLockOnClassAndReadClockOnTestCaseFromProvider.class
				)
		);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("doesntSetForceExecutionModeForChildrenWithReadLocksOnClassAndReadLockOnTestCases")
	void doesntSetForceExecutionModeForChildrenWithReadLocksOnClassAndReadLockOnTest(Class<?> testCase) {
		var engineDescriptor = discover(testCase);

		var advisor = nodeTreeWalker.walk(engineDescriptor);

		var testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ), getReadLock("a"), getReadLock("b")));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		var testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()).isEqualTo(List.of());
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).isEmpty();
	}

	static Stream<?> leavesResourceLockOnTestMethodWhenClassDoesNotUseResourceCases() {
		// @formatter:off
		return Stream.of(
				Named.of("locks from annotation", TestCaseWithoutResourceLockFromAnnotation.class),
				Named.of("locks from provider", TestCaseWithoutResourceLockFromProvider.class)
		);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("leavesResourceLockOnTestMethodWhenClassDoesNotUseResourceCases")
	void leavesResourceLockOnTestMethodWhenClassDoesNotUseResource(Class<?> testCase) {
		var engineDescriptor = discover(testCase);

		var advisor = nodeTreeWalker.walk(engineDescriptor);

		var testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ)));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		assertThat(testClassDescriptor.getChildren()).hasSize(2);
		var children = testClassDescriptor.getChildren().iterator();
		var testMethodDescriptor = children.next();
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getReadWriteLock("a")));
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).isEmpty();

		var nestedTestClassDescriptor = children.next();
		assertThat(advisor.getResourceLock(nestedTestClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getReadWriteLock("b"), getReadWriteLock("c")));
		assertThat(advisor.getForcedExecutionMode(nestedTestClassDescriptor)).isEmpty();

		var nestedTestMethodDescriptor = getOnlyElement(nestedTestClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(nestedTestMethodDescriptor)).extracting(allLocks()).isEqualTo(List.of());
		assertThat(advisor.getForcedExecutionMode(nestedTestMethodDescriptor)).contains(SAME_THREAD);
	}

	static Stream<?> coarsensGlobalLockToEngineDescriptorChildCases() {
		// @formatter:off
		return Stream.of(
				Named.of("locks from annotation", TestCaseWithGlobalLockRequiringChildFromAnnotation.class),
				Named.of("locks from provider", TestCaseWithGlobalLockRequiringChildFromProvider.class)
		);
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("coarsensGlobalLockToEngineDescriptorChildCases")
	void coarsensGlobalLockToEngineDescriptorChild(Class<?> testCase) {
		var engineDescriptor = discover(testCase);

		var advisor = nodeTreeWalker.walk(engineDescriptor);

		var testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ_WRITE)));
		assertThat(advisor.getForcedExecutionMode(testClassDescriptor)).isEmpty();

		var nestedTestClassDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(nestedTestClassDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ)));
		assertThat(advisor.getForcedExecutionMode(nestedTestClassDescriptor)).contains(SAME_THREAD);

		var testMethodDescriptor = getOnlyElement(nestedTestClassDescriptor.getChildren());
		assertThat(advisor.getResourceLock(testMethodDescriptor)).extracting(allLocks()) //
				.isEqualTo(List.of(getLock(GLOBAL_READ_WRITE)));
		assertThat(advisor.getForcedExecutionMode(testMethodDescriptor)).contains(SAME_THREAD);
	}

	private static Function<org.junit.platform.engine.support.hierarchical.ResourceLock, List<Lock>> allLocks() {
		return ResourceLockSupport::getLocks;
	}

	private Lock getReadWriteLock(String key) {
		return getLock(new ExclusiveResource(key, READ_WRITE));
	}

	private Lock getReadLock(String key) {
		return getLock(new ExclusiveResource(key, READ));
	}

	private Lock getLock(ExclusiveResource exclusiveResource) {
		return getOnlyElement(ResourceLockSupport.getLocks(lockManager.getLockForResource(exclusiveResource)));
	}

	private TestDescriptor discover(Class<?> testClass) {
		var discoveryRequest = request().selectors(selectClass(testClass)).build();
		return new JupiterTestEngine().discover(discoveryRequest, UniqueId.forEngine("junit-jupiter"));
	}

	@ResourceLock("a")
	static class TestCaseWithResourceLockFromAnnotation {
		@Test
		@ResourceLock("b")
		void test() {
		}
	}

	@ResourceLocksFrom(TestCaseWithResourceLockFromProvider.Provider.class)
	static class TestCaseWithResourceLockFromProvider {
		@Test
		void test() {
		}

		static final class Provider implements ResourceLocksProvider {
			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a"));
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				return Set.of(new Lock("b"));
			}
		}
	}

	static class TestCaseWithoutResourceLockFromAnnotation {
		@Test
		@ResourceLock("a")
		void test() {
		}

		@Nested
		@ResourceLock("c")
		class NestedTestCaseWithResourceLock {
			@Test
			@ResourceLock("b")
			void test() {
			}
		}
	}

	@ResourceLocksFrom(TestCaseWithoutResourceLockFromProvider.Provider.class)
	static class TestCaseWithoutResourceLockFromProvider {
		@Test
		void test() {
		}

		@Nested
		@ResourceLocksFrom(TestCaseWithoutResourceLockFromProvider.Provider.class)
		class NestedTestCaseWithResourceLock {
			@Test
			void test() {
			}
		}

		static final class Provider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				return Set.of(new Lock("c"));
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				if (testClass == TestCaseWithoutResourceLockFromProvider.class) {
					return Set.of(new Lock("a"));
				}
				else if (testClass == TestCaseWithoutResourceLockFromProvider.NestedTestCaseWithResourceLock.class) {
					return Set.of(new Lock("b"));
				}
				return Set.of();
			}
		}
	}

	static class TestCaseWithGlobalLockRequiringChildFromAnnotation {
		@Nested
		class NestedTestCaseWithResourceLock {
			@Test
			@ResourceLock(ExclusiveResource.GLOBAL_KEY)
			void test() {
			}
		}
	}

	static class TestCaseWithGlobalLockRequiringChildFromProvider {
		@Nested
		@ResourceLocksFrom(TestCaseWithGlobalLockRequiringChildFromProvider.Provider.class)
		class NestedTestCaseWithResourceLock {
			@Test
			void test() {
			}
		}

		static final class Provider implements ResourceLocksProvider {
			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				return Set.of(new Lock(ExclusiveResource.GLOBAL_KEY));
			}
		}
	}

	@ResourceLock("a")
	static class TestCaseWithResourceWriteLockOnClassFromAnnotation {
		@Test
		void test() {
		}
	}

	@ResourceLocksFrom(TestCaseWithResourceWriteLockOnClassFromProvider.Provider.class)
	static class TestCaseWithResourceWriteLockOnClassFromProvider {
		@Test
		void test() {
		}

		static final class Provider implements ResourceLocksProvider {
			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a"));
			}
		}
	}

	@ResourceLock(value = "a", mode = ResourceAccessMode.READ)
	static class TestCaseWithResourceReadLockOnClassFromAnnotation {
		@Test
		void test() {
		}
	}

	@ResourceLocksFrom(TestCaseWithResourceReadLockOnClassFromProvider.Provider.class)
	static class TestCaseWithResourceReadLockOnClassFromProvider {
		@Test
		void test() {
		}

		static final class Provider implements ResourceLocksProvider {
			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a", ResourceAccessMode.READ));
			}
		}
	}

	@ResourceLock(value = "a", mode = ResourceAccessMode.READ)
	static class TestCaseWithResourceReadLockOnClassAndWriteClockOnTestCaseFromAnnotation {
		@Test
		@ResourceLock("a")
		void test() {
		}
	}

	@ResourceLocksFrom(TestCaseWithResourceReadLockOnClassAndWriteClockOnTestCaseFromProvider.Provider.class)
	static class TestCaseWithResourceReadLockOnClassAndWriteClockOnTestCaseFromProvider {
		@Test
		void test() {
		}

		static final class Provider implements ResourceLocksProvider {
			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a", ResourceAccessMode.READ));
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				return Set.of(new Lock("a"));
			}
		}
	}

	@ResourceLock(value = "a", mode = ResourceAccessMode.READ)
	static class TestCaseWithResourceReadLockOnClassAndReadClockOnTestCaseFromAnnotation {
		@Test
		@ResourceLock(value = "b", mode = ResourceAccessMode.READ)
		void test() {
		}
	}

	@ResourceLocksFrom(TestCaseWithResourceReadLockOnClassAndReadClockOnTestCaseFromProvider.Provider.class)
	static class TestCaseWithResourceReadLockOnClassAndReadClockOnTestCaseFromProvider {
		@Test
		void test() {
		}

		static final class Provider implements ResourceLocksProvider {
			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a", ResourceAccessMode.READ));
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				return Set.of(new Lock("b", ResourceAccessMode.READ));
			}
		}
	}

}
