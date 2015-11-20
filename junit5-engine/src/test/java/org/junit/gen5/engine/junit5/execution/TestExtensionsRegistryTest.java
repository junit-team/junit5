/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.engine.junit5.extension.TestNameParameterResolver;

public class TestExtensionsRegistryTest {

	private TestExtensionsRegistry registry;

	@Test
	public void checkJUnit5DefaultExtensions() {
		Assert.assertEquals(1, TestExtensionsRegistry.getDefaultExtensionClasses().size());

		assertDefaultExtensionType(TestNameParameterResolver.class);
	}

	@Test
	public void newRegistryWithoutParentHasDefaultExtensions() {
		registry = new TestExtensionsRegistry();
		List<? extends TestExtension> extensions = registry.getExtensions();

		Assert.assertEquals(TestExtensionsRegistry.getDefaultExtensionClasses().size(), extensions.size());
		assertExtensionPresent(TestNameParameterResolver.class);
	}

	@Test
	public void addNewExtensionByClass() {

		registry = new TestExtensionsRegistry();
		registry.addExtensionFromClass(MyExtension.class);

		assertExtensionPresent(MyExtension.class);

		int rememberSize = registry.getExtensions().size();
		registry.addExtensionFromClass(MyExtension.class);
		registry.addExtensionFromClass(MyExtension.class);
		registry.addExtensionFromClass(MyExtension.class);
		Assert.assertEquals(rememberSize, registry.getExtensions().size());
	}

	@Test
	public void extensionsAreInheritedFromParent() {

		TestExtensionsRegistry parent = new TestExtensionsRegistry();
		parent.addExtensionFromClass(MyExtension.class);

		registry = new TestExtensionsRegistry(parent);
		registry.addExtensionFromClass(YourExtension.class);

		assertExtensionPresent(MyExtension.class);
		assertExtensionPresent(YourExtension.class);

		TestExtensionsRegistry grandChild = new TestExtensionsRegistry(registry);
		assertExtensionPresentIn(MyExtension.class, grandChild.getExtensions());
	}

	private void assertExtensionPresent(Class<? extends TestExtension> extensionClass) {
		List<TestExtension> extensions = registry.getExtensions();
		assertExtensionPresentIn(extensionClass, extensions);
	}

	private void assertExtensionPresentIn(Class<? extends TestExtension> extensionClass,
			List<TestExtension> extensions) {
		String assertionMessage = extensionClass.getSimpleName() + " should be present";
		Assert.assertTrue(assertionMessage, extensions.stream().anyMatch(extension -> {
			return extension.getClass().equals(extensionClass);
		}));
	}

	private void assertDefaultExtensionType(Class<TestNameParameterResolver> defaultExtension) {
		Assert.assertTrue(defaultExtension.getName() + " should be default extension",
			TestExtensionsRegistry.getDefaultExtensionClasses().contains(defaultExtension));
	}

}

class MyExtension implements TestExtension {
}

class YourExtension implements TestExtension {
}
