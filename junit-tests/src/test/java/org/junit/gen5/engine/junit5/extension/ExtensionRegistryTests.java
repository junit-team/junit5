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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.BeforeEach;
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

	private ExtensionRegistry registry;

	@BeforeEach
	public void initRegistry() {
		registry = new ExtensionRegistry();
	}

	@Test
	public void checkDefaultExtensions() {
		Assertions.assertEquals(3, ExtensionRegistry.getDefaultExtensionTypes().size());

		assertDefaultExtensionType(DisabledCondition.class);
		assertDefaultExtensionType(TestInfoParameterResolver.class);
	}

	@Test
	public void newRegistryWithoutParentHasDefaultExtensions() throws Exception {
		Set<Class<? extends Extension>> extensions = registry.getRegisteredExtensionTypes();

		assertEquals(ExtensionRegistry.getDefaultExtensionTypes().size(), extensions.size());
		assertExtensionRegistered(registry, DisabledCondition.class);
		assertExtensionRegistered(registry, TestInfoParameterResolver.class);
		assertExtensionRegistered(registry, TestReporterParameterResolver.class);

		assertEquals(2, countExtensionPoints(MethodParameterResolver.class));
		assertEquals(1, countExtensionPoints(ContainerExecutionCondition.class));
		assertEquals(1, countExtensionPoints(TestExecutionCondition.class));
	}

	@Test
	public void registerExtensionByImplementingClass() throws Exception {

		registry.registerExtension(MyExtension.class);

		assertExtensionRegistered(registry, MyExtension.class);

		int rememberSize = registry.getRegisteredExtensionTypes().size();
		registry.registerExtension(MyExtension.class);
		registry.registerExtension(MyExtension.class);
		registry.registerExtension(MyExtension.class);

		assertEquals(rememberSize, registry.getRegisteredExtensionTypes().size());
		assertExtensionRegistered(registry, MyExtension.class);
		assertEquals(1, countExtensionPoints(MyExtensionPoint.class));

		registry.registerExtension(YourExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensionPoints(MyExtensionPoint.class));
	}

	@Test
	public void registerTestExtensionThatImplementsMultipleExtensionPoints() throws Exception {

		registry.registerExtension(MultipleExtension.class);

		assertExtensionRegistered(registry, MultipleExtension.class);

		assertEquals(1, countExtensionPoints(MyExtensionPoint.class));
		assertEquals(1, countExtensionPoints(AnotherExtensionPoint.class));
	}

	@Test
	public void extensionsAreInheritedFromParent() throws Exception {

		ExtensionRegistry parent = new ExtensionRegistry();
		parent.registerExtension(MyExtension.class);

		registry = new ExtensionRegistry(parent);
		registry.registerExtension(YourExtension.class);

		assertExtensionRegistered(registry, MyExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensionPoints(MyExtensionPoint.class));

		ExtensionRegistry grandChild = new ExtensionRegistry(registry);
		assertExtensionRegistered(grandChild, MyExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensionPoints(MyExtensionPoint.class));
	}

	@Test
	public void registerExtensionPointsByExtensionRegistrar() throws Exception {

		registry.registerExtension(MyExtensionRegistrar.class);

		assertExtensionRegistered(registry, MyExtensionRegistrar.class);
		assertEquals(1, countExtensionPoints(MyExtensionPoint.class));
		assertEquals(1, countExtensionPoints(AnotherExtensionPoint.class));
	}

	@Test
	public void canStreamOverRegisteredExceptionPoint() throws Exception {

		registry.registerExtension(MyExtension.class);

		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.stream(MyExtensionPoint.class, ExtensionPointRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> {
				assertEquals(MyExtension.class.getName(), registeredExtensionPoint.getSource().getClass().getName());
				assertEquals(Position.DEFAULT, registeredExtensionPoint.getPosition());
				hasRun.set(true);
			});

		assertTrue(hasRun.get());

	}

	@Test
	public void registerExtensionPointFromLambdaExpression() throws Exception {
		registry.registerExtensionPoint((MyExtensionPoint) test -> {
		}, this);

		assertBehaviorForExtensionPointRegisteredFromLambdaExpressionOrMethodReference();
	}

	@Test
	public void registerExtensionPointFromMethodReference() throws Exception {
		registry.registerExtensionPoint((MyExtensionPoint) this::consumeString, this);
		assertBehaviorForExtensionPointRegisteredFromLambdaExpressionOrMethodReference();
	}

	/**
	 * "Implements" MyExtensionPoint.
	 */
	private void consumeString(String test) {
		/* no-op */
	}

	private void assertBehaviorForExtensionPointRegisteredFromLambdaExpressionOrMethodReference() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.stream(MyExtensionPoint.class, ExtensionPointRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> {
				Class<? extends MyExtensionPoint> lambdaType = registeredExtensionPoint.getExtensionPoint().getClass();
				assertTrue(lambdaType.getName().contains("$Lambda$"));
				assertEquals(getClass().getName(), registeredExtensionPoint.getSource().getClass().getName());
				assertEquals(Position.DEFAULT, registeredExtensionPoint.getPosition());
				hasRun.set(true);
			});

		assertTrue(hasRun.get());
	}

	private int countExtensionPoints(Class<? extends ExtensionPoint> extensionPointType) throws Exception {
		AtomicInteger counter = new AtomicInteger();
		registry.stream(extensionPointType, ExtensionPointRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> counter.incrementAndGet());
		return counter.get();
	}

	private void assertExtensionRegistered(ExtensionRegistry registry, Class<? extends Extension> extensionClass) {
		assertTrue(registry.getRegisteredExtensionTypes().contains(extensionClass),
			() -> extensionClass.getSimpleName() + " should be present");
	}

	private void assertDefaultExtensionType(Class<?> extensionType) {
		assertTrue(ExtensionRegistry.getDefaultExtensionTypes().contains(extensionType),
			() -> extensionType.getName() + " should be a default extension");
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
