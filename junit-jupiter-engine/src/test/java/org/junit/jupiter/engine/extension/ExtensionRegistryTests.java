/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionCondition;

/**
 * @since 5.0
 */
public class ExtensionRegistryTests {

	@Test
	void newRegistryWithoutParentHasDefaultExtensions() {
		ExtensionRegistry registry = ExtensionRegistry.createRegistryWithDefaultExtensions();
		Set<Class<? extends Extension>> extensions = registry.getRegisteredExtensionTypes();

		assertEquals(3, extensions.size());
		assertExtensionRegistered(registry, DisabledCondition.class);
		assertExtensionRegistered(registry, TestInfoParameterResolver.class);
		assertExtensionRegistered(registry, TestReporterParameterResolver.class);

		assertEquals(2, countExtensions(registry, ParameterResolver.class));
		assertEquals(1, countExtensions(registry, ContainerExecutionCondition.class));
		assertEquals(1, countExtensions(registry, TestExecutionCondition.class));
	}

	@Test
	void registerExtensionByImplementingClass() {
		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MyExtension.class);

		assertExtensionRegistered(registry, MyExtension.class);

		registry.registerExtension(MyExtension.class);
		registry.registerExtension(MyExtension.class);
		registry.registerExtension(MyExtension.class);

		assertEquals(1, registry.getRegisteredExtensionTypes().size());
		assertExtensionRegistered(registry, MyExtension.class);
		assertEquals(1, countExtensions(registry, MyExtensionApi.class));

		registry.registerExtension(YourExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensions(registry, MyExtensionApi.class));
	}

	@Test
	void registerExtensionThatImplementsMultipleExtensionApis() {
		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MultipleExtension.class);

		assertExtensionRegistered(registry, MultipleExtension.class);

		assertEquals(1, countExtensions(registry, MyExtensionApi.class));
		assertEquals(1, countExtensions(registry, AnotherExtensionApi.class));
	}

	@Test
	void extensionsAreInheritedFromParent() {
		ExtensionRegistry parent = new ExtensionRegistry(Optional.empty());
		parent.registerExtension(MyExtension.class);

		ExtensionRegistry child = ExtensionRegistry.createRegistryFrom(parent, singletonList(YourExtension.class));
		assertExtensionRegistered(child, MyExtension.class);
		assertExtensionRegistered(child, YourExtension.class);
		assertEquals(2, countExtensions(child, MyExtensionApi.class));

		ExtensionRegistry grandChild = new ExtensionRegistry(Optional.of(child));
		assertExtensionRegistered(grandChild, MyExtension.class);
		assertExtensionRegistered(grandChild, YourExtension.class);
		assertEquals(2, countExtensions(grandChild, MyExtensionApi.class));
	}

	@Test
	void canStreamOverRegisteredExtension() {
		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MyExtension.class);

		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.getExtensions(MyExtensionApi.class).forEach(extension -> {
			assertEquals(MyExtension.class.getName(), extension.getClass().getName());
			hasRun.set(true);
		});

		assertTrue(hasRun.get());
	}

	@Test
	void registerExtensionFromLambdaExpression() {
		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension((MyExtensionApi) test -> {
		}, this);

		assertBehaviorForExtensionRegisteredFromLambdaExpressionOrMethodReference(registry);
	}

	@Test
	void registerExtensionFromMethodReference() {
		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension((MyExtensionApi) this::consumeString, this);
		assertBehaviorForExtensionRegisteredFromLambdaExpressionOrMethodReference(registry);
	}

	/**
	 * "Implements" MyExtensionApi.
	 */
	private void consumeString(String test) {
		/* no-op */
	}

	private void assertBehaviorForExtensionRegisteredFromLambdaExpressionOrMethodReference(ExtensionRegistry registry) {
		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.getRegisteredExtensions(MyExtensionApi.class).forEach(registeredExtension -> {
			Class<? extends MyExtensionApi> lambdaType = registeredExtension.getExtension().getClass();
			assertTrue(lambdaType.getName().contains("$Lambda$"));
			assertEquals(getClass().getName(), registeredExtension.getSource().getClass().getName());
			hasRun.set(true);
		});

		assertTrue(hasRun.get());
	}

	private long countExtensions(ExtensionRegistry registry, Class<? extends Extension> extensionType) {
		return registry.stream(extensionType).count();
	}

	private void assertExtensionRegistered(ExtensionRegistry registry, Class<? extends Extension> extensionType) {
		assertTrue(registry.getRegisteredExtensionTypes().contains(extensionType),
			() -> extensionType.getSimpleName() + " should be present");
	}

	// -------------------------------------------------------------------------

	interface MyExtensionApi extends Extension {

		void doNothing(String test);
	}

	interface AnotherExtensionApi extends Extension {

		void doMore();
	}

	static class MyExtension implements MyExtensionApi {

		@Override
		public void doNothing(String test) {
		}
	}

	static class YourExtension implements MyExtensionApi {

		@Override
		public void doNothing(String test) {
		}
	}

	static class MultipleExtension implements MyExtensionApi, AnotherExtensionApi {

		@Override
		public void doNothing(String test) {
		}

		@Override
		public void doMore() {
		}
	}

}
