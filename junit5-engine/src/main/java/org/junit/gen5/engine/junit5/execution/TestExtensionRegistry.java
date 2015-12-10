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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.extension.DisabledCondition;
import org.junit.gen5.engine.junit5.extension.TestNameParameterResolver;

/**
 * @since 5.0
 */
public class TestExtensionRegistry {

	private static final List<Class<? extends TestExtension>> defaultExtensionClasses = Collections.unmodifiableList(
		Arrays.asList(DisabledCondition.class, TestNameParameterResolver.class));

	static List<Class<? extends TestExtension>> getDefaultExtensionClasses() {
		return defaultExtensionClasses;
	}

	private final Set<Class<? extends TestExtension>> registeredExtensionClasses = new LinkedHashSet<>();

	private final List<RegisteredExtensionPoint> registeredExtensionPoints = new ArrayList<>();

	private final Optional<TestExtensionRegistry> parent;

	public TestExtensionRegistry() {
		this(null);
	}

	public TestExtensionRegistry(TestExtensionRegistry parent) {
		this.parent = Optional.ofNullable(parent);
		if (!this.parent.isPresent()) {
			addDefaultExtensions();
		}
	}

	private void addDefaultExtensions() {
		// @formatter:off
		getDefaultExtensionClasses().stream()
			.forEach(extensionClass -> addExtension(extensionClass));
		// @formatter:on
	}

	public Set<Class<? extends TestExtension>> getRegisteredExtensionClasses() {
		Set<Class<? extends TestExtension>> allRegisteredExtensionClasses = new LinkedHashSet<>();
		this.parent.ifPresent(
			parentRegistry -> allRegisteredExtensionClasses.addAll(parentRegistry.getRegisteredExtensionClasses()));
		allRegisteredExtensionClasses.addAll(this.registeredExtensionClasses);
		return Collections.unmodifiableSet(allRegisteredExtensionClasses);
	}

	public <T extends ExtensionPoint> Stream<T> getExtensionPoints(Class<T> extensionClass) {
		//TODO: Consider Position
		//TODO: Consider reverse order for AfterExtensionPoints
		//TODO: Get all extension points from parent
		//@formatter:off
		return registeredExtensionPoints.stream()
				.filter(registeredExtensionPoint -> extensionClass.isAssignableFrom(registeredExtensionPoint.extensionPoint.getClass()))
				.map(registeredExtensionPoint -> extensionClass.cast(registeredExtensionPoint.extensionPoint));
		//@formatter:on
	}

	public void addExtension(Class<? extends TestExtension> extensionClass) {
		boolean extensionExists = getRegisteredExtensionClasses().stream().anyMatch(
			registeredClass -> registeredClass.equals(extensionClass));
		if (!extensionExists) {
			TestExtension testExtension = ReflectionUtils.newInstance(extensionClass);
			if (testExtension instanceof ExtensionPoint) {
				register((ExtensionPoint) testExtension, ExtensionPoint.Position.DEFAULT);
			}
			this.registeredExtensionClasses.add(extensionClass);
		}
	}

	public <T extends ExtensionPoint> void register(T extensionPoint, ExtensionPoint.Position position) {
		RegisteredExtensionPoint registeredExtensionPoint = new RegisteredExtensionPoint(extensionPoint, position);
		registeredExtensionPoints.add(registeredExtensionPoint);
	}

	private static class RegisteredExtensionPoint {
		private final ExtensionPoint extensionPoint;
		private final ExtensionPoint.Position position;

		private RegisteredExtensionPoint(ExtensionPoint extensionPoint, ExtensionPoint.Position position) {
			this.extensionPoint = extensionPoint;
			this.position = position;
		}
	}
}
