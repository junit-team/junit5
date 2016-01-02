/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.api.Assertions.*;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ContainerExecutionCondition;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPoint.Position;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.api.extension.ExtensionRegistry;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.TestExecutionCondition;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.engine.junit5.extension.DisabledCondition;
import org.junit.gen5.engine.junit5.extension.TestNameParameterResolver;

public class TestExtensionRegistryTests {

	private TestExtensionRegistry registry;

	@BeforeEach
	public void initRegistry() {
		registry = new TestExtensionRegistry();
	}

	@Test
	public void checkDefaultExtensions() {
		assertEquals(2, TestExtensionRegistry.getDefaultExtensionClasses().size());

		assertDefaultExtensionType(DisabledCondition.class);
		assertDefaultExtensionType(TestNameParameterResolver.class);
	}

	@Test
	public void newRegistryWithoutParentHasDefaultExtensions() throws Exception {
		Set<Class<? extends TestExtension>> extensions = registry.getRegisteredExtensionClasses();

		assertEquals(TestExtensionRegistry.getDefaultExtensionClasses().size(), extensions.size());
		assertExtensionRegistered(registry, DisabledCondition.class);
		assertExtensionRegistered(registry, TestNameParameterResolver.class);

		assertEquals(1, countExtensionPoints(MethodParameterResolver.class));
		assertEquals(1, countExtensionPoints(ContainerExecutionCondition.class));
		assertEquals(1, countExtensionPoints(TestExecutionCondition.class));
	}

	@Test
	public void registerExtensionByImplementingClass() throws Exception {

		registry.addExtension(MyExtension.class);

		assertExtensionRegistered(registry, MyExtension.class);

		int rememberSize = registry.getRegisteredExtensionClasses().size();
		registry.addExtension(MyExtension.class);
		registry.addExtension(MyExtension.class);
		registry.addExtension(MyExtension.class);

		assertEquals(rememberSize, registry.getRegisteredExtensionClasses().size());
		assertExtensionRegistered(registry, MyExtension.class);
		assertEquals(1, countExtensionPoints(MyExtensionPoint.class));

		registry.addExtension(YourExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensionPoints(MyExtensionPoint.class));
	}

	@Test
	public void registerTestExtensionThatImplementsMultipleExtensionPoints() throws Exception {

		registry.addExtension(MultipleExtension.class);

		assertExtensionRegistered(registry, MultipleExtension.class);

		assertEquals(1, countExtensionPoints(MyExtensionPoint.class));
		assertEquals(1, countExtensionPoints(AnotherExtensionPoint.class));
	}

	@Test
	public void extensionsAreInheritedFromParent() throws Exception {

		TestExtensionRegistry parent = new TestExtensionRegistry();
		parent.addExtension(MyExtension.class);

		registry = new TestExtensionRegistry(parent);
		registry.addExtension(YourExtension.class);

		assertExtensionRegistered(registry, MyExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensionPoints(MyExtensionPoint.class));

		TestExtensionRegistry grandChild = new TestExtensionRegistry(registry);
		assertExtensionRegistered(grandChild, MyExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensionPoints(MyExtensionPoint.class));
	}

	@Test
	public void registerExtensionPointsByExtensionRegistrar() throws Exception {

		registry.addExtension(MyExtensionRegistrar.class);

		assertExtensionRegistered(registry, MyExtensionRegistrar.class);
		assertEquals(1, countExtensionPoints(MyExtensionPoint.class));
		assertEquals(1, countExtensionPoints(AnotherExtensionPoint.class));
	}

	@Test
	public void canStreamOverRegisteredExceptionPoint() throws Exception {

		registry.addExtension(MyExtension.class);

		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.stream(MyExtensionPoint.class, TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> {
				assertEquals(MyExtension.class.getName(), registeredExtensionPoint.getExtensionName());
				assertEquals(Position.DEFAULT, registeredExtensionPoint.getPosition());
				assertTrue(registeredExtensionPoint.getExtensionPoint() instanceof MyExtensionPoint);
				hasRun.set(true);
			});

		assertTrue(hasRun.get());

	}

	@Test
	public void registerExtensionPointDirectly() throws Exception {

		registry.registerExtension((MyExtensionPoint) test -> {
		}, Position.INNERMOST, "anonymous extension");

		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.stream(MyExtensionPoint.class, TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> {
				assertEquals("anonymous extension", registeredExtensionPoint.getExtensionName());
				assertEquals(Position.INNERMOST, registeredExtensionPoint.getPosition());
				assertTrue(registeredExtensionPoint.getExtensionPoint() instanceof MyExtensionPoint);
				hasRun.set(true);
			});

		assertTrue(hasRun.get());

	}

	private int countExtensionPoints(Class<? extends ExtensionPoint> extensionPointType) throws Exception {
		AtomicInteger counter = new AtomicInteger();
		registry.stream(extensionPointType, TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> counter.incrementAndGet());
		return counter.get();
	}

	private void assertExtensionRegistered(TestExtensionRegistry registry,
			Class<? extends TestExtension> extensionClass) {
		assertTrue(registry.getRegisteredExtensionClasses().contains(extensionClass),
			() -> extensionClass.getSimpleName() + " should be present");
	}

	private void assertDefaultExtensionType(Class<?> extensionType) {
		assertTrue(TestExtensionRegistry.getDefaultExtensionClasses().contains(extensionType),
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
	public void registerExtensions(ExtensionRegistry registry) {
		registry.register(this::doNothing, MyExtensionPoint.class);
		registry.register(this::doMore, AnotherExtensionPoint.class);
	}

	private void doMore() {
	}

	private void doNothing(String s) {
	}
}
