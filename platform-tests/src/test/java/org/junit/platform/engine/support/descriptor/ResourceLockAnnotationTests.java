/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;

/**
 * Integration tests for {@link ResourceLock} and {@link ResourceLocksProvider}.
 *
 * @since 5.12
 */
class ResourceLockAnnotationTests {

	private static final UniqueId uniqueId = UniqueId.root("enigma", "foo");

	private final JupiterConfiguration configuration = mock();

	@BeforeEach
	void setUp() {
		when(configuration.getDefaultDisplayNameGenerator()).thenReturn(new DisplayNameGenerator.Standard());
		when(configuration.getDefaultExecutionMode()).thenReturn(ExecutionMode.SAME_THREAD);
	}

	@Test
	void noSharedResources() {
		// @formatter:off
		var classResources = getClassResources(
				NoSharedResourcesTestCase.class
		);
		assertThat(classResources).isEmpty();

		var methodResources = getMethodResources(
				NoSharedResourcesTestCase.class
		);
		assertThat(methodResources).isEmpty();

		var nestedClassResources = getNestedClassResources(
				NoSharedResourcesTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).isEmpty();
		// @formatter:on
	}

	@Test
	void addSharedResourcesViaAnnotationValue() {
		// @formatter:off
		var classResources = getClassResources(
				SharedResourcesViaAnnotationValueTestCase.class
		);
		assertThat(classResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a1", LockMode.READ_WRITE),
				new ExclusiveResource("a2", LockMode.READ_WRITE)
		);

		var methodResources = getMethodResources(
				SharedResourcesViaAnnotationValueTestCase.class
		);
		assertThat(methodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("b1", LockMode.READ),
				new ExclusiveResource("b2", LockMode.READ_WRITE)
		);

		var nestedClassResources = getNestedClassResources(
				SharedResourcesViaAnnotationValueTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).containsExactlyInAnyOrder(
				new ExclusiveResource("c1", LockMode.READ),
				new ExclusiveResource("c2", LockMode.READ)
		);
		// @formatter:on
	}

	@Test
	void addSharedResourcesViaAnnotationProviders() {
		// @formatter:off
		var classResources = getClassResources(
				SharedResourcesViaAnnotationProvidersTestCase.class
		);
		assertThat(classResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a1", LockMode.READ),
				new ExclusiveResource("a2", LockMode.READ)
		);

		var methodResources = getMethodResources(
				SharedResourcesViaAnnotationProvidersTestCase.class
		);
		assertThat(methodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("b1", LockMode.READ_WRITE),
				new ExclusiveResource("b2", LockMode.READ_WRITE)
		);

		var nestedClassResources = getNestedClassResources(
				SharedResourcesViaAnnotationProvidersTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).containsExactlyInAnyOrder(
				new ExclusiveResource("c1", LockMode.READ_WRITE),
				new ExclusiveResource("c2", LockMode.READ)
		);
		// @formatter:on
	}

	@Test
	void addSharedResourcesViaAnnotationValueAndProviders() {
		// @formatter:off
		var classResources = getClassResources(
				SharedResourcesViaAnnotationValueAndProvidersTestCase.class
		);
		assertThat(classResources).containsExactlyInAnyOrder(
				new ExclusiveResource("a1", LockMode.READ_WRITE),
				new ExclusiveResource("a2", LockMode.READ)
		);

		var methodResources = getMethodResources(
				SharedResourcesViaAnnotationValueAndProvidersTestCase.class
		);
		assertThat(methodResources).containsExactlyInAnyOrder(
				new ExclusiveResource("b1", LockMode.READ),
				new ExclusiveResource("b2", LockMode.READ)
		);

		var nestedClassResources = getNestedClassResources(
				SharedResourcesViaAnnotationValueAndProvidersTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).containsExactlyInAnyOrder(
				new ExclusiveResource("c1", LockMode.READ_WRITE),
				new ExclusiveResource("c2", LockMode.READ_WRITE)
		);
		// @formatter:on
	}

	@Test
	void emptyAnnotation() {
		// @formatter:off
		var classResources = getClassResources(
				EmptyAnnotationTestCase.class
		);
		assertThat(classResources).isEmpty();

		var methodResources = getMethodResources(
				EmptyAnnotationTestCase.class
		);
		assertThat(methodResources).isEmpty();

		var nestedClassResources = getNestedClassResources(
				EmptyAnnotationTestCase.NestedClass.class
		);
		assertThat(nestedClassResources).isEmpty();
		// @formatter:on
	}

	private Set<ExclusiveResource> getClassResources(Class<?> testClass) {
		return new ClassTestDescriptor(uniqueId, testClass, configuration).getExclusiveResources();
	}

	private Set<ExclusiveResource> getMethodResources(Class<?> testClass) {
		try {
			// @formatter:off
			var descriptor = new TestMethodTestDescriptor(
				uniqueId, testClass, testClass.getDeclaredMethod("test"), configuration
			);
			// @formatter:on
			descriptor.setParent(new ClassTestDescriptor(uniqueId, testClass, configuration));
			return descriptor.getExclusiveResources();
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<ExclusiveResource> getNestedClassResources(Class<?> testClass) {
		return new NestedClassTestDescriptor(uniqueId, testClass, configuration).getExclusiveResources();
	}

	// -------------------------------------------------------------------------

	static class NoSharedResourcesTestCase {

		@Test
		void test() {
		}

		@Nested
		class NestedClass {
		}
	}

	@ResourceLock("a1")
	@ResourceLock(value = "a2", mode = ResourceAccessMode.READ_WRITE)
	static class SharedResourcesViaAnnotationValueTestCase {

		@Test
		@ResourceLock(value = "b1", mode = ResourceAccessMode.READ)
		@ResourceLock("b2")
		void test() {
		}

		@Nested
		@ResourceLock(value = "c1", mode = ResourceAccessMode.READ)
		@ResourceLock(value = "c2", mode = ResourceAccessMode.READ)
		class NestedClass {
		}
	}

	@ResourceLock(providers = { //
			SharedResourcesViaAnnotationProvidersTestCase.FirstClassLevelProvider.class, //
			SharedResourcesViaAnnotationProvidersTestCase.SecondClassLevelProvider.class //
	})
	static class SharedResourcesViaAnnotationProvidersTestCase {

		@Test
		@ResourceLock(providers = MethodLevelProvider.class)
		void test() {
		}

		@Nested
		@ResourceLock(providers = NestedClassLevelProvider.class)
		class NestedClass {
		}

		static class FirstClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a1", ResourceAccessMode.READ));
			}
		}

		static class SecondClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a2", ResourceAccessMode.READ));
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				return Set.of(new Lock("b1"));
			}
		}

		static class MethodLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				return Set.of(new Lock("b2"));
			}
		}

		static class NestedClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				return Set.of(new Lock("c1"), new Lock("c2", ResourceAccessMode.READ));
			}
		}
	}

	@ResourceLock( //
			value = "a1", //
			mode = ResourceAccessMode.READ_WRITE, //
			providers = SharedResourcesViaAnnotationValueAndProvidersTestCase.ClassLevelProvider.class //
	)
	static class SharedResourcesViaAnnotationValueAndProvidersTestCase {

		@Test
		@ResourceLock(value = "b1", mode = ResourceAccessMode.READ)
		void test() {
		}

		@Nested
		@ResourceLock("c1")
		@ResourceLock(providers = NestedClassLevelProvider.class)
		class NestedClass {
		}

		static class ClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return Set.of(new Lock("a2", ResourceAccessMode.READ));
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				return Set.of(new Lock("b2", ResourceAccessMode.READ));
			}
		}

		static class NestedClassLevelProvider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				return Set.of(new Lock("c2"));
			}
		}
	}

	@ResourceLock
	static class EmptyAnnotationTestCase {

		@Test
		@ResourceLock
		void test() {
		}

		@Nested
		@ResourceLock
		class NestedClass {
		}
	}

	static class NestedNestedTestCase {

		@Nested
		@ResourceLock(providers = NestedNestedTestCase.Provider.class)
		static class NestedClass {

			@Nested
			class NestedClassTwo {

				@Test
				void test() {
				}
			}
		}

		static class Provider implements ResourceLocksProvider {
			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				return ResourceLocksProvider.super.provideForClass(testClass);
			}

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				return ResourceLocksProvider.super.provideForNestedClass(testClass);
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				return ResourceLocksProvider.super.provideForMethod(testClass, testMethod);
			}
		}
	}

}
