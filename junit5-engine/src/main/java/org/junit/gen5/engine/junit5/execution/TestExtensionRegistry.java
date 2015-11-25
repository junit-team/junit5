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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.extension.DisabledCondition;
import org.junit.gen5.engine.junit5.extension.TestNameParameterResolver;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public class TestExtensionRegistry {

	private static final List<Class<? extends TestExtension>> defaultExtensionClasses = Collections.unmodifiableList(
		Arrays.asList(DisabledCondition.class, TestNameParameterResolver.class));

	static List<Class<? extends TestExtension>> getDefaultExtensionClasses() {
		return defaultExtensionClasses;
	}

	private final Set<TestExtension> extensions = new LinkedHashSet<>();

	private final Optional<TestExtensionRegistry> parent;

	TestExtensionRegistry() {
		this(null);
	}

	TestExtensionRegistry(TestExtensionRegistry parent) {
		this.parent = Optional.ofNullable(parent);
		if (!this.parent.isPresent()) {
			addDefaultExtensions();
		}
	}

	private void addDefaultExtensions() {
		// @formatter:off
		getDefaultExtensionClasses().stream()
			.map(extensionClass -> ReflectionUtils.newInstance(extensionClass))
			.forEach(this.extensions::add);
		// @formatter:on
	}

	public Set<TestExtension> getExtensions() {
		Set<TestExtension> allExtensions = new LinkedHashSet<>();
		this.parent.ifPresent(parentRegistry -> allExtensions.addAll(parentRegistry.getExtensions()));
		allExtensions.addAll(this.extensions);
		return Collections.unmodifiableSet(allExtensions);
	}

	public void addExtension(Class<? extends TestExtension> extensionClass) {
		boolean extensionExists = getExtensions().stream().anyMatch(
			extension -> extension.getClass().equals(extensionClass));
		if (!extensionExists) {
			this.extensions.add(ReflectionUtils.newInstance(extensionClass));
		}
	}

}
