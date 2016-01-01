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

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class TestExtensionRegistryTest {

	private TestExtensionRegistry registry;

	@Before
	public void initRegistry() {
		registry = new TestExtensionRegistry();
	}

	@Test
	public void checkDefaultExtensions() {
		Assert.assertEquals(2, TestExtensionRegistry.getDefaultExtensionClasses().size());

		assertDefaultExtensionType(DisabledCondition.class);
		assertDefaultExtensionType(TestNameParameterResolver.class);
	}

	@Test
	public void newRegistryWithoutParentHasDefaultExtensions() throws Throwable {
		Set<Class<? extends TestExtension>> extensions = registry.getRegisteredExtensionClasses();

		Assert.assertEquals(TestExtensionRegistry.getDefaultExtensionClasses().size(), extensions.size());
		assertExtensionRegistered(registry, DisabledCondition.class);
		assertExtensionRegistered(registry, TestNameParameterResolver.class);

		Assert.assertEquals(1, countExtensionPoints(MethodParameterResolver.class));
		Assert.assertEquals(1, countExtensionPoints(ContainerExecutionCondition.class));
		Assert.assertEquals(1, countExtensionPoints(TestExecutionCondition.class));
	}

	@Test
	public void registerExtensionByImplementingClass() throws Throwable {

		registry.addExtension(MyExtension.class);

		assertExtensionRegistered(registry, MyExtension.class);

		int rememberSize = registry.getRegisteredExtensionClasses().size();
		registry.addExtension(MyExtension.class);
		registry.addExtension(MyExtension.class);
		registry.addExtension(MyExtension.class);

		Assert.assertEquals(rememberSize, registry.getRegisteredExtensionClasses().size());
		assertExtensionRegistered(registry, MyExtension.class);
		Assert.assertEquals(1, countExtensionPoints(MyExtensionPoint.class));

		registry.addExtension(YourExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		Assert.assertEquals(2, countExtensionPoints(MyExtensionPoint.class));
	}

	@Test
	public void registerTestExtensionThatImplementsMultipleExtensionPoints() throws Throwable {

		registry.addExtension(MultipleExtension.class);

		assertExtensionRegistered(registry, MultipleExtension.class);

		Assert.assertEquals(1, countExtensionPoints(MyExtensionPoint.class));
		Assert.assertEquals(1, countExtensionPoints(AnotherExtensionPoint.class));
	}

	@Test
	public void extensionsAreInheritedFromParent() throws Throwable {

		TestExtensionRegistry parent = new TestExtensionRegistry();
		parent.addExtension(MyExtension.class);

		registry = new TestExtensionRegistry(parent);
		registry.addExtension(YourExtension.class);

		assertExtensionRegistered(registry, MyExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		Assert.assertEquals(2, countExtensionPoints(MyExtensionPoint.class));

		TestExtensionRegistry grandChild = new TestExtensionRegistry(registry);
		assertExtensionRegistered(grandChild, MyExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		Assert.assertEquals(2, countExtensionPoints(MyExtensionPoint.class));
	}

	@Test
	public void registerExtensionPointsByExtensionRegistrar() throws Throwable {

		registry.addExtension(MyExtensionRegistrar.class);

		assertExtensionRegistered(registry, MyExtensionRegistrar.class);
		Assert.assertEquals(1, countExtensionPoints(MyExtensionPoint.class));
		Assert.assertEquals(1, countExtensionPoints(AnotherExtensionPoint.class));
	}

	@Test
	public void canStreamOverRegisteredExceptionPoint() throws Throwable {

		registry.addExtension(MyExtension.class);

		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.stream(MyExtensionPoint.class, TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> {
				Assert.assertEquals(MyExtension.class.getName(), registeredExtensionPoint.getExtensionName());
				Assert.assertEquals(Position.DEFAULT, registeredExtensionPoint.getPosition());
				Assert.assertTrue(registeredExtensionPoint.getExtensionPoint() instanceof MyExtensionPoint);
				hasRun.set(true);
			});

		Assert.assertTrue(hasRun.get());

	}

	@Test
	public void registerExtensionPointDirectly() throws Throwable {

		registry.registerExtension((MyExtensionPoint) test -> {
		}, Position.INNERMOST, "anonymous extension");

		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.stream(MyExtensionPoint.class, TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> {
				Assert.assertEquals("anonymous extension", registeredExtensionPoint.getExtensionName());
				Assert.assertEquals(Position.INNERMOST, registeredExtensionPoint.getPosition());
				Assert.assertTrue(registeredExtensionPoint.getExtensionPoint() instanceof MyExtensionPoint);
				hasRun.set(true);
			});

		Assert.assertTrue(hasRun.get());

	}

	private int countExtensionPoints(Class<? extends ExtensionPoint> extensionPointType) throws Throwable {
		AtomicInteger counter = new AtomicInteger();
		registry.stream(extensionPointType, TestExtensionRegistry.ApplicationOrder.FORWARD).forEach(
			registeredExtensionPoint -> counter.incrementAndGet());
		return counter.get();
	}

	private void assertExtensionRegistered(TestExtensionRegistry registry,
			Class<? extends TestExtension> extensionClass) {
		String assertionMessage = extensionClass.getSimpleName() + " should be present";
		Assert.assertTrue(assertionMessage, registry.getRegisteredExtensionClasses().contains(extensionClass));
	}

	private void assertDefaultExtensionType(Class<?> extensionType) {
		Assert.assertTrue(extensionType.getName() + " should be a default extension",
			TestExtensionRegistry.getDefaultExtensionClasses().contains(extensionType));
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
