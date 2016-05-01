/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static java.util.Collections.singletonList;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ContainerExecutionCondition;
import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.ExtensionPointRegistry.Position;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.TestExecutionCondition;

/**
 * @since 5.0
 */
public class ExtensionRegistryTests {

	@Test
	public void newRegistryWithoutParentHasDefaultExtensions() throws Exception {
		ExtensionRegistry registry = ExtensionRegistry.newRootRegistryWithDefaultExtensions();
		Set<Class<? extends Extension>> extensions = registry.getRegisteredExtensionTypes();

		assertEquals(3, extensions.size());
		assertExtensionRegistered(registry, DisabledCondition.class);
		assertExtensionRegistered(registry, TestInfoParameterResolver.class);
		assertExtensionRegistered(registry, TestReporterParameterResolver.class);

		assertEquals(2, countExtensions(registry, MethodParameterResolver.class));
		assertEquals(1, countExtensions(registry, ContainerExecutionCondition.class));
		assertEquals(1, countExtensions(registry, TestExecutionCondition.class));
	}

	@Test
	public void registerExtensionByImplementingClass() throws Exception {

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
	public void registerTestExtensionThatImplementsMultipleExtensions() throws Exception {

		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MultipleExtension.class);

		assertExtensionRegistered(registry, MultipleExtension.class);

		assertEquals(1, countExtensions(registry, MyExtensionApi.class));
		assertEquals(1, countExtensions(registry, AnotherExtensionApi.class));
	}

	@Test
	public void extensionsAreInheritedFromParent() throws Exception {

		ExtensionRegistry parent = new ExtensionRegistry(Optional.empty());
		parent.registerExtension(MyExtension.class);

		ExtensionRegistry child = ExtensionRegistry.newRegistryFrom(parent, singletonList(YourExtension.class));
		assertExtensionRegistered(child, MyExtension.class);
		assertExtensionRegistered(child, YourExtension.class);
		assertEquals(2, countExtensions(child, MyExtensionApi.class));

		ExtensionRegistry grandChild = new ExtensionRegistry(Optional.of(child));
		assertExtensionRegistered(grandChild, MyExtension.class);
		assertExtensionRegistered(grandChild, YourExtension.class);
		assertEquals(2, countExtensions(grandChild, MyExtensionApi.class));
	}

	@Test
	public void registerExtensionsByExtensionRegistrar() throws Exception {

		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MyExtensionRegistrar.class);

		assertExtensionRegistered(registry, MyExtensionRegistrar.class);
		assertEquals(1, countExtensions(registry, MyExtensionApi.class));
		assertEquals(1, countExtensions(registry, AnotherExtensionApi.class));
	}

	@Test
	public void canStreamOverRegisteredExtension() throws Exception {

		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MyExtension.class);

		AtomicBoolean hasRun = new AtomicBoolean(false);

		stream(registry, MyExtensionApi.class).forEach(registeredExtension -> {
			assertEquals(MyExtension.class.getName(), registeredExtension.getSource().getClass().getName());
			assertEquals(Position.DEFAULT, registeredExtension.getPosition());
			hasRun.set(true);
		});

		assertTrue(hasRun.get());
	}

	@Test
	public void registerExtensionFromLambdaExpression() throws Exception {
		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension((MyExtensionApi) test -> {
		}, this);

		assertBehaviorForExtensionRegisteredFromLambdaExpressionOrMethodReference(registry);
	}

	@Test
	public void registerExtensionFromMethodReference() throws Exception {
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

	private void assertBehaviorForExtensionRegisteredFromLambdaExpressionOrMethodReference(ExtensionRegistry registry)
			throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean(false);

		stream(registry, MyExtensionApi.class).forEach(registeredExtension -> {
			Class<? extends MyExtensionApi> lambdaType = registeredExtension.getExtension().getClass();
			assertTrue(lambdaType.getName().contains("$Lambda$"));
			assertEquals(getClass().getName(), registeredExtension.getSource().getClass().getName());
			assertEquals(Position.DEFAULT, registeredExtension.getPosition());
			hasRun.set(true);
		});

		assertTrue(hasRun.get());
	}

	private <E extends Extension> Stream<RegisteredExtension<E>> stream(ExtensionRegistry registry,
			Class<E> extensionType) {
		return registry.getRegisteredExtensions(extensionType).stream();
	}

	private long countExtensions(ExtensionRegistry registry, Class<? extends Extension> extensionType) {
		return registry.stream(extensionType).count();
	}

	private void assertExtensionRegistered(ExtensionRegistry registry, Class<? extends Extension> extensionClass) {
		assertTrue(registry.getRegisteredExtensionTypes().contains(extensionClass),
			() -> extensionClass.getSimpleName() + " should be present");
	}

}

interface MyExtensionApi extends Extension {

	void doNothing(String test);

}

interface AnotherExtensionApi extends Extension {

	void doMore();

}

class MyExtension implements MyExtensionApi {

	@Override
	public void doNothing(String test) {
	}

}

class YourExtension implements MyExtensionApi {

	@Override
	public void doNothing(String test) {
	}

}

class MultipleExtension implements MyExtensionApi, AnotherExtensionApi {

	@Override
	public void doNothing(String test) {
	}

	@Override
	public void doMore() {
	}

}

class MyExtensionRegistrar implements ExtensionRegistrar {

	@Override
	public void registerExtensions(ExtensionPointRegistry registry) {
		registry.register((MyExtensionApi) this::doNothing);
		registry.register((AnotherExtensionApi) this::doMore);
	}

	private void doMore() {
	}

	private void doNothing(String s) {
	}

}
