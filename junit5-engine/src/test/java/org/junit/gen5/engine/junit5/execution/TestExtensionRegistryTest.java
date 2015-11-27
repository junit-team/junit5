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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.engine.junit5.extension.DisabledCondition;
import org.junit.gen5.engine.junit5.extension.ForAllExtension;
import org.junit.gen5.engine.junit5.extension.TestNameParameterResolver;

/**
 * @since 5.0
 */
public class TestExtensionRegistryTest {

	private TestExtensionRegistry registry;

	@Test
	public void checkJUnit5DefaultExtensions() {
		Assert.assertEquals(3, TestExtensionRegistry.getDefaultExtensionClasses().size());

		assertDefaultExtensionType(DisabledCondition.class);
		assertDefaultExtensionType(TestNameParameterResolver.class);
		assertDefaultExtensionType(ForAllExtension.class);
	}

	@Test
	public void newRegistryWithoutParentHasDefaultExtensions() {
		registry = new TestExtensionRegistry();
		Set<? extends TestExtension> extensions = registry.getExtensions();

		Assert.assertEquals(TestExtensionRegistry.getDefaultExtensionClasses().size(), extensions.size());
		assertExtensionPresent(DisabledCondition.class);
		assertExtensionPresent(TestNameParameterResolver.class);
	}

	@Test
	public void addNewExtensionByClass() {

		registry = new TestExtensionRegistry();
		registry.addExtension(MyExtension.class);

		assertExtensionPresent(MyExtension.class);

		int rememberSize = registry.getExtensions().size();
		registry.addExtension(MyExtension.class);
		registry.addExtension(MyExtension.class);
		registry.addExtension(MyExtension.class);
		Assert.assertEquals(rememberSize, registry.getExtensions().size());
	}

	@Test
	public void extensionsAreInheritedFromParent() {

		TestExtensionRegistry parent = new TestExtensionRegistry();
		parent.addExtension(MyExtension.class);

		registry = new TestExtensionRegistry(parent);
		registry.addExtension(YourExtension.class);

		assertExtensionPresent(MyExtension.class);
		assertExtensionPresent(YourExtension.class);

		TestExtensionRegistry grandChild = new TestExtensionRegistry(registry);
		assertExtensionPresentIn(MyExtension.class, grandChild.getExtensions());
	}

	private void assertExtensionPresent(Class<? extends TestExtension> extensionClass) {
		assertExtensionPresentIn(extensionClass, registry.getExtensions());
	}

	private void assertExtensionPresentIn(Class<? extends TestExtension> extensionClass,
			Set<TestExtension> extensions) {
		String assertionMessage = extensionClass.getSimpleName() + " should be present";
		Assert.assertTrue(assertionMessage,
			extensions.stream().anyMatch(extension -> extension.getClass().equals(extensionClass)));
	}

	private void assertDefaultExtensionType(Class<?> extensionType) {
		Assert.assertTrue(extensionType.getName() + " should be a default extension",
			TestExtensionRegistry.getDefaultExtensionClasses().contains(extensionType));
	}

}

class MyExtension implements TestExtension {
}

class YourExtension implements TestExtension {
}
