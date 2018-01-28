/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.engine.Constants;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * Tests for the {@link ExtensionRegistry}.
 *
 * @since 5.0
 */
class ExtensionRegistryTests {

	private final ConfigurationParameters configParams = mock(ConfigurationParameters.class);

	private ExtensionRegistry registry = createRegistryWithDefaultExtensions(configParams);

	@Test
	void newRegistryWithoutParentHasDefaultExtensions() {
		List<Extension> extensions = registry.getExtensions(Extension.class);

		assertEquals(5, extensions.size());
		assertDefaultGlobalExtensionsAreRegistered();
	}

	@Test
	void newRegistryWithoutParentHasDefaultExtensionsPlusAutodetectedExtensionsLoadedViaServiceLoader() {

		when(configParams.getBoolean(Constants.EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME)).thenReturn(
			Optional.of(Boolean.TRUE));
		registry = createRegistryWithDefaultExtensions(configParams);

		List<Extension> extensions = registry.getExtensions(Extension.class);

		assertEquals(6, extensions.size());
		assertDefaultGlobalExtensionsAreRegistered();

		assertExtensionRegistered(registry, ServiceLoaderExtension.class);
		assertEquals(1, countExtensions(registry, BeforeAllCallback.class));
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

	private void assertDefaultGlobalExtensionsAreRegistered() {
		assertExtensionRegistered(registry, DisabledCondition.class);
		assertExtensionRegistered(registry, ScriptExecutionCondition.class);
		assertExtensionRegistered(registry, RepeatedTestExtension.class);
		assertExtensionRegistered(registry, TestInfoParameterResolver.class);
		assertExtensionRegistered(registry, TestReporterParameterResolver.class);

		assertEquals(2, countExtensions(registry, ParameterResolver.class));
		assertEquals(2, countExtensions(registry, ExecutionCondition.class));
		assertEquals(1, countExtensions(registry, TestTemplateInvocationContextProvider.class));
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
