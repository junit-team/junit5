/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.extension.ExtensionRegistry.createRegistryFrom;
import static org.junit.jupiter.engine.extension.ExtensionRegistry.createRegistryWithDefaultExtensions;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExecutionCondition;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

/**
 * @since 5.0
 */
public class ExtensionRegistryTests {

	private final ExtensionRegistry registry = createRegistryWithDefaultExtensions();

	@Test
	void newRegistryWithoutParentHasDefaultExtensions() {
		List<Extension> extensions = registry.getExtensions(Extension.class);

		assertEquals(4, extensions.size());
		assertExtensionRegistered(registry, DisabledCondition.class);
		assertExtensionRegistered(registry, RepeatedTestExtension.class);
		assertExtensionRegistered(registry, TestInfoParameterResolver.class);
		assertExtensionRegistered(registry, TestReporterParameterResolver.class);

		assertEquals(2, countExtensions(registry, ParameterResolver.class));
		assertEquals(1, countExtensions(registry, ContainerExecutionCondition.class));
		assertEquals(1, countExtensions(registry, TestExecutionCondition.class));
		assertEquals(1, countExtensions(registry, TestTemplateInvocationContextProvider.class));
	}

	@Test
	void registerExtensionByImplementingClass() {
		registry.registerExtension(MyExtension.class);

		assertExtensionRegistered(registry, MyExtension.class);

		registry.registerExtension(MyExtension.class);
		registry.registerExtension(MyExtension.class);
		registry.registerExtension(MyExtension.class);

		assertEquals(1, registry.getExtensions(MyExtension.class).size());
		assertExtensionRegistered(registry, MyExtension.class);
		assertEquals(1, countExtensions(registry, MyExtensionApi.class));

		registry.registerExtension(YourExtension.class);
		assertExtensionRegistered(registry, YourExtension.class);
		assertEquals(2, countExtensions(registry, MyExtensionApi.class));
	}

	@Test
	void registerExtensionThatImplementsMultipleExtensionApis() {
		registry.registerExtension(MultipleExtension.class);

		assertExtensionRegistered(registry, MultipleExtension.class);

		assertEquals(1, countExtensions(registry, MyExtensionApi.class));
		assertEquals(1, countExtensions(registry, AnotherExtensionApi.class));
	}

	@Test
	void extensionsAreInheritedFromParent() {
		ExtensionRegistry parent = registry;
		parent.registerExtension(MyExtension.class);

		ExtensionRegistry child = createRegistryFrom(parent, singletonList(YourExtension.class));
		assertExtensionRegistered(child, MyExtension.class);
		assertExtensionRegistered(child, YourExtension.class);
		assertEquals(2, countExtensions(child, MyExtensionApi.class));

		ExtensionRegistry grandChild = createRegistryFrom(child, emptyList());
		assertExtensionRegistered(grandChild, MyExtension.class);
		assertExtensionRegistered(grandChild, YourExtension.class);
		assertEquals(2, countExtensions(grandChild, MyExtensionApi.class));
	}

	@Test
	void registeringSameExtensionImplementationInParentAndChildDoesNotResultInDuplicate() {
		ExtensionRegistry parent = registry;
		parent.registerExtension(MyExtension.class);
		assertEquals(1, countExtensions(parent, MyExtensionApi.class));

		ExtensionRegistry child = createRegistryFrom(parent, asList(MyExtension.class, YourExtension.class));
		assertExtensionRegistered(child, MyExtension.class);
		assertExtensionRegistered(child, YourExtension.class);
		assertEquals(2, countExtensions(child, MyExtensionApi.class));

		ExtensionRegistry grandChild = createRegistryFrom(child, asList(MyExtension.class, YourExtension.class));
		assertExtensionRegistered(grandChild, MyExtension.class);
		assertExtensionRegistered(grandChild, YourExtension.class);
		assertEquals(2, countExtensions(grandChild, MyExtensionApi.class));
	}

	@Test
	void canStreamOverRegisteredExtension() {
		registry.registerExtension(MyExtension.class);

		AtomicBoolean hasRun = new AtomicBoolean(false);

		registry.getExtensions(MyExtensionApi.class).forEach(extension -> {
			assertEquals(MyExtension.class.getName(), extension.getClass().getName());
			hasRun.set(true);
		});

		assertTrue(hasRun.get());
	}

	private long countExtensions(ExtensionRegistry registry, Class<? extends Extension> extensionType) {
		return registry.stream(extensionType).count();
	}

	private void assertExtensionRegistered(ExtensionRegistry registry, Class<? extends Extension> extensionType) {
		assertFalse(registry.getExtensions(extensionType).isEmpty(),
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
