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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ContainerExecutionCondition;
import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.api.extension.ExtensionPoint;
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

		assertEquals(2, countExtensionPoints(registry, MethodParameterResolver.class));
		assertEquals(1, countExtensionPoints(registry, ContainerExecutionCondition.class));
		assertEquals(1, countExtensionPoints(registry, TestExecutionCondition.class));
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
		assertEquals(1, countExtensionPoints(registry, MyExtensionPoint.class));

		registry.registerExtension(YourExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensionPoints(registry, MyExtensionPoint.class));
	}

	@Test
	public void registerTestExtensionThatImplementsMultipleExtensionPoints() throws Exception {

		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MultipleExtension.class);

		assertExtensionRegistered(registry, MultipleExtension.class);

		assertEquals(1, countExtensionPoints(registry, MyExtensionPoint.class));
		assertEquals(1, countExtensionPoints(registry, AnotherExtensionPoint.class));
	}

	@Test
	public void extensionsAreInheritedFromParent() throws Exception {

		ExtensionRegistry parent = new ExtensionRegistry(Optional.empty());
		parent.registerExtension(MyExtension.class);

		ExtensionRegistry child = ExtensionRegistry.newRegistryFrom(parent, singletonList(YourExtension.class));
		assertExtensionRegistered(child, MyExtension.class);
		assertExtensionRegistered(child, YourExtension.class);
		assertEquals(2, countExtensionPoints(child, MyExtensionPoint.class));

		ExtensionRegistry grandChild = new ExtensionRegistry(Optional.of(child));
		assertExtensionRegistered(grandChild, MyExtension.class);
		assertExtensionRegistered(grandChild, YourExtension.class);
		assertEquals(2, countExtensionPoints(grandChild, MyExtensionPoint.class));
	}

	@Test
	public void registerExtensionPointsByExtensionRegistrar() throws Exception {

		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MyExtensionRegistrar.class);

		assertExtensionRegistered(registry, MyExtensionRegistrar.class);
		assertEquals(1, countExtensionPoints(registry, MyExtensionPoint.class));
		assertEquals(1, countExtensionPoints(registry, AnotherExtensionPoint.class));
	}

	@Test
	public void canStreamOverRegisteredExtensionPoint() throws Exception {

		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtension(MyExtension.class);

		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.stream(MyExtensionPoint.class, ExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> {
				assertEquals(MyExtension.class.getName(), registeredExtensionPoint.getSource().getClass().getName());
				assertEquals(Position.DEFAULT, registeredExtensionPoint.getPosition());
				hasRun.set(true);
			});

		assertTrue(hasRun.get());
	}

	@Test
	public void registerExtensionPointFromLambdaExpression() throws Exception {
		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtensionPoint((MyExtensionPoint) test -> {
		}, this);

		assertBehaviorForExtensionPointRegisteredFromLambdaExpressionOrMethodReference(registry);
	}

	@Test
	public void registerExtensionPointFromMethodReference() throws Exception {
		ExtensionRegistry registry = new ExtensionRegistry(Optional.empty());
		registry.registerExtensionPoint((MyExtensionPoint) this::consumeString, this);
		assertBehaviorForExtensionPointRegisteredFromLambdaExpressionOrMethodReference(registry);
	}

	/**
	 * "Implements" MyExtensionPoint.
	 */
	private void consumeString(String test) {
		/* no-op */
	}

	private void assertBehaviorForExtensionPointRegisteredFromLambdaExpressionOrMethodReference(
			ExtensionRegistry registry) throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.stream(MyExtensionPoint.class, ExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> {
				Class<? extends MyExtensionPoint> lambdaType = registeredExtensionPoint.getExtensionPoint().getClass();
				assertTrue(lambdaType.getName().contains("$Lambda$"));
				assertEquals(getClass().getName(), registeredExtensionPoint.getSource().getClass().getName());
				assertEquals(Position.DEFAULT, registeredExtensionPoint.getPosition());
				hasRun.set(true);
			});

		assertTrue(hasRun.get());
	}

	private int countExtensionPoints(ExtensionRegistry registry, Class<? extends ExtensionPoint> extensionPointType)
			throws Exception {
		AtomicInteger counter = new AtomicInteger();
		registry.stream(extensionPointType, ExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> counter.incrementAndGet());
		return counter.get();
	}

	private void assertExtensionRegistered(ExtensionRegistry registry, Class<? extends Extension> extensionClass) {
		assertTrue(registry.getRegisteredExtensionTypes().contains(extensionClass),
			() -> extensionClass.getSimpleName() + " should be present");
	}

}

interface MyExtensionPoint extends ExtensionPoint {

	void doNothing(String test);
}

interface AnotherExtensionPoint extends ExtensionPoint {

	void doMore();
}

class MyExtension implements MyExtensionPoint {

	@Override
	public void doNothing(String test) {

	}
}

class YourExtension implements MyExtensionPoint {

	@Override
	public void doNothing(String test) {

	}
}

class MultipleExtension implements MyExtensionPoint, AnotherExtensionPoint {

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
		registry.register((MyExtensionPoint) this::doNothing);
		registry.register((AnotherExtensionPoint) this::doMore);
	}

	private void doMore() {
	}

	private void doNothing(String s) {
	}
}
