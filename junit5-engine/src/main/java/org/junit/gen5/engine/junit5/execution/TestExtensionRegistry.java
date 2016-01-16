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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.extension.DisabledCondition;
import org.junit.gen5.engine.junit5.extension.TestInfoParameterResolver;
import org.junit.gen5.engine.junit5.extension.TestReporterParameterResolver;

/**
 * A {@code TestExtensionRegistry} holds all registered extensions (i.e.
 * instances of {@link ExtensionPoint}) for a given
 * {@link org.junit.gen5.engine.Container} or {@link org.junit.gen5.engine.Leaf}.
 *
 * <p>A registry has a reference to its parent registry, and all lookups are
 * performed first in the current registry itself and then in its parent and
 * thereby all its ancestors.
 *
 * @since 5.0
 * @see ExtensionPointRegistry
 * @see ExtensionRegistrar
 */
public class TestExtensionRegistry implements ExtensionPointRegistry {

	public enum ApplicationOrder {
		FORWARD, BACKWARD
	}

	/**
	 * Factory for creating and populating a new registry from a list of
	 * extension types and a parent registry.
	 *
	 * @param parentRegistry the parent registry to be used
	 * @param extensionTypes the types of extensions to be registered in
	 * the new registry
	 * @return a new {@code TestExtensionRegistry}
	 */
	public static TestExtensionRegistry newRegistryFrom(TestExtensionRegistry parentRegistry,
			List<Class<? extends Extension>> extensionTypes) {

		TestExtensionRegistry newTestExtensionRegistry = new TestExtensionRegistry(parentRegistry);
		extensionTypes.forEach(newTestExtensionRegistry::registerExtension);
		return newTestExtensionRegistry;
	}

	private static final List<Class<? extends Extension>> defaultExtensionTypes = Collections.unmodifiableList(
		Arrays.asList(DisabledCondition.class, TestInfoParameterResolver.class, TestReporterParameterResolver.class));

	/**
	 * @return the list of all extension types that are added by default to all root registries
	 */
	static List<Class<? extends Extension>> getDefaultExtensionTypes() {
		return defaultExtensionTypes;
	}

	private final Set<Class<? extends Extension>> registeredExtensionTypes = new LinkedHashSet<>();

	private final List<RegisteredExtensionPoint<?>> registeredExtensionPoints = new ArrayList<>();

	private final Optional<TestExtensionRegistry> parent;

	public TestExtensionRegistry() {
		this(null);
	}

	TestExtensionRegistry(TestExtensionRegistry parent) {
		this.parent = Optional.ofNullable(parent);
		if (!this.parent.isPresent()) {
			addDefaultExtensions();
		}
	}

	private void addDefaultExtensions() {
		getDefaultExtensionTypes().stream().forEach(this::registerExtension);
	}

	/**
	 * @return all extension types registered in this registry or one of its ancestors
	 */
	Set<Class<? extends Extension>> getRegisteredExtensionTypes() {
		Set<Class<? extends Extension>> allRegisteredExtensionTypes = new LinkedHashSet<>();
		this.parent.ifPresent(
			parentRegistry -> allRegisteredExtensionTypes.addAll(parentRegistry.getRegisteredExtensionTypes()));
		allRegisteredExtensionTypes.addAll(this.registeredExtensionTypes);
		return Collections.unmodifiableSet(allRegisteredExtensionTypes);
	}

	@SuppressWarnings("unchecked")
	private <E extends ExtensionPoint> List<RegisteredExtensionPoint<E>> getRegisteredExtensionPoints(
			Class<E> extensionType) {

		List<RegisteredExtensionPoint<E>> allExtensionPoints = new ArrayList<>();
		this.parent.ifPresent(
			parentRegistry -> allExtensionPoints.addAll(parentRegistry.getRegisteredExtensionPoints(extensionType)));

		// @formatter:off
		this.registeredExtensionPoints.stream()
				.filter(registeredExtensionPoint -> extensionType.isAssignableFrom(registeredExtensionPoint.getExtensionPoint().getClass()))
				.forEach(extensionPoint -> allExtensionPoints.add((RegisteredExtensionPoint<E>) extensionPoint));
		// @formatter:on

		return allExtensionPoints;
	}

	/**
	 * Return a stream for iterating over all registered extension points
	 * of the specified type.
	 *
	 * @param extensionPointType the type of {@link ExtensionPoint} to stream
	 * @param order the order in which to apply the extension points after sorting
	 */
	public <E extends ExtensionPoint> Stream<RegisteredExtensionPoint<E>> stream(Class<E> extensionPointType,
			ApplicationOrder order) {

		List<RegisteredExtensionPoint<E>> registeredExtensionPoints = getRegisteredExtensionPoints(extensionPointType);
		new ExtensionPointSorter().sort(registeredExtensionPoints);
		if (order == ApplicationOrder.BACKWARD) {
			Collections.reverse(registeredExtensionPoints);
		}
		return registeredExtensionPoints.stream();
	}

	/**
	 * Instantiate an extension of the given type using its default constructor,
	 * and potentially register it in this registry.
	 *
	 * <p>If the extension is an {@link ExtensionPoint}, it will be registered
	 * in this registry, unless an extension of the given type already exists
	 * in this registry.
	 *
	 * <p>If the extension is an {@link ExtensionRegistrar},
	 * its {@link ExtensionRegistrar#registerExtensions registerExtensions()}
	 * method will be invoked to register extensions from the registrar in
	 * this registry.
	 *
	 * @param extensionType the type extension to register
	 */
	void registerExtension(Class<? extends Extension> extensionType) {

		boolean extensionAlreadyRegistered = getRegisteredExtensionTypes().stream().anyMatch(
			registeredType -> registeredType.equals(extensionType));

		if (!extensionAlreadyRegistered) {
			Extension extension = ReflectionUtils.newInstance(extensionType);
			registerExtensionPoint(extension);
			registerExtensionPointsFromRegistrar(extension);
			this.registeredExtensionTypes.add(extensionType);
		}
	}

	private void registerExtensionPoint(Extension extension) {
		if (extension instanceof ExtensionPoint) {
			registerExtensionPoint((ExtensionPoint) extension);
		}
	}

	public void registerExtensionPoint(ExtensionPoint extension) {
		registerExtensionPoint(extension, Position.DEFAULT);
	}

	void registerExtensionPoint(ExtensionPoint extension, Position position) {
		this.registeredExtensionPoints.add(new RegisteredExtensionPoint<>(extension, position));
	}

	private void registerExtensionPointsFromRegistrar(Extension extension) {
		if (extension instanceof ExtensionRegistrar) {
			ExtensionRegistrar extensionRegistrar = (ExtensionRegistrar) extension;
			extensionRegistrar.registerExtensions(this);
		}
	}

	@Override
	public <E extends ExtensionPoint> void register(E extension, Class<E> extensionPointType, Position position) {
		// TODO Document why extensionPointType is not used or unnecessary.
		registerExtensionPoint(extension, position);
	}

}
