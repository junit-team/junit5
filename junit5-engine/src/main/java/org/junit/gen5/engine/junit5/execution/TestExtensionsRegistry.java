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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.extension.TestNameParameterResolver;

public class TestExtensionsRegistry {

	static private List<Class<? extends TestExtension>> defaultExtensionsClasses = null;

	public static List<Class<? extends TestExtension>> getDefaultExtensionClasses() {
		if (defaultExtensionsClasses == null) {
			initializeDefaultExtensionClasses();
		}
		return Collections.unmodifiableList(defaultExtensionsClasses);
	}

	private static void initializeDefaultExtensionClasses() {
		defaultExtensionsClasses = new ArrayList<>();
		defaultExtensionsClasses.add(TestNameParameterResolver.class);
	}

	private final List<TestExtension> extensions = new ArrayList<>();
	private final Optional<TestExtensionsRegistry> parent;

	TestExtensionsRegistry() {
		this(null);
	}

	TestExtensionsRegistry(TestExtensionsRegistry parentValue) {
		parent = Optional.ofNullable(parentValue);
		if (!parent.isPresent()) {
			addDefaultExtensions();
		}
	}

	private void addDefaultExtensions() {
		getDefaultExtensionClasses().stream().map(
			extensionClass -> ReflectionUtils.newInstance(extensionClass)).forEach(
				(TestExtension extension) -> extensions.add(extension));
	}

	public List<TestExtension> getExtensions() {
		List<TestExtension> allExtensions = new ArrayList<>(extensions);
		parent.ifPresent(parentRegistry -> allExtensions.addAll(parentRegistry.getExtensions()));
		return Collections.unmodifiableList(allExtensions);
	}

	public void addExtensionFromClass(Class<? extends TestExtension> extensionClass) {
		boolean extensionExists = getExtensions().stream().anyMatch(
			extension -> extension.getClass().equals(extensionClass));
		if (!extensionExists) {
			TestExtension extensionInstance = ReflectionUtils.newInstance(extensionClass);
			extensions.add(extensionInstance);
		}

	}
}
