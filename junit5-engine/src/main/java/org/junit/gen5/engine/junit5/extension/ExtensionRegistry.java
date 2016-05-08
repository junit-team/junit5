/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * An {@code ExtensionRegistry} holds all registered extensions (i.e.
 * instances of {@link Extension}) for a given
 * {@link org.junit.gen5.engine.support.hierarchical.Container} or
 * {@link org.junit.gen5.engine.support.hierarchical.Leaf}.
 *
 * <p>A registry has a reference to its parent registry, and all lookups are
 * performed first in the current registry itself and then recursively in its
 * ancestors.
 *
 * @since 5.0
 */
@API(Internal)
public class ExtensionRegistry {

	private static final Logger LOG = Logger.getLogger(ExtensionRegistry.class.getName());

	private static final List<Class<? extends Extension>> DEFAULT_EXTENSIONS = Collections.unmodifiableList(
		Arrays.asList(DisabledCondition.class, TestInfoParameterResolver.class, TestReporterParameterResolver.class));

	/**
	 * Factory for creating a new empty root registry.
	 *
	 * @return a new {@code ExtensionRegistry}
	 */
	public static ExtensionRegistry createEmptyRegistry() {
		return new ExtensionRegistry(Optional.empty());
	}

	/**
	 * Factory for creating and populating a new root registry with the default extension types.
	 *
	 * @return a new {@code ExtensionRegistry}
	 */
	public static ExtensionRegistry createRegistryWithDefaultExtensions() {
		ExtensionRegistry extensionRegistry = new ExtensionRegistry(Optional.empty());
		DEFAULT_EXTENSIONS.forEach(extensionRegistry::registerExtension);
		return extensionRegistry;
	}

	/**
	 * Factory for creating and populating a new registry from a list of
	 * extension types and a parent registry.
	 *
	 * @param parentRegistry the parent registry
	 * @param extensionTypes the types of extensions to be registered in
	 * the new registry
	 * @return a new {@code ExtensionRegistry}
	 */
	public static ExtensionRegistry createRegistryFrom(ExtensionRegistry parentRegistry,
			List<Class<? extends Extension>> extensionTypes) {

		Preconditions.notNull(parentRegistry, "parentRegistry must not be null");

		ExtensionRegistry registry = new ExtensionRegistry(Optional.of(parentRegistry));
		extensionTypes.forEach(registry::registerExtension);
		return registry;
	}

	private final Optional<ExtensionRegistry> parent;

	private final Set<Class<? extends Extension>> registeredExtensionTypes = new LinkedHashSet<>();

	private final List<RegisteredExtension<?>> registeredExtensions = new ArrayList<>();

	ExtensionRegistry(Optional<ExtensionRegistry> parent) {
		this.parent = parent;
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

	/**
	 * @return all {@code RegisteredExtensions} in this registry or one of its ancestors
	 */
	@SuppressWarnings("unchecked")
	<E extends Extension> List<RegisteredExtension<E>> getRegisteredExtensions(Class<E> extensionType) {

		List<RegisteredExtension<E>> extensions = new ArrayList<>();
		this.parent.ifPresent(
			parentRegistry -> extensions.addAll(parentRegistry.getRegisteredExtensions(extensionType)));

		// @formatter:off
		this.registeredExtensions.stream()
				.filter(registeredExtension -> extensionType.isAssignableFrom(registeredExtension.getExtension().getClass()))
				.forEach(extension -> extensions.add((RegisteredExtension<E>) extension));
		// @formatter:on

		return extensions;
	}

	/**
	 * Generate a stream for iterating over all registered extensions of the
	 * specified type.
	 *
	 * @param extensionType the type of {@link Extension} to stream
	 */
	public <E extends Extension> Stream<E> stream(Class<E> extensionType) {
		return stream(extensionType, false);
	}

	/**
	 * Generate a stream for iterating over all registered extensions of the
	 * specified type in reverse order.
	 *
	 * @param extensionType the type of {@link Extension} to stream
	 */
	public <E extends Extension> Stream<E> reverseStream(Class<E> extensionType) {
		return stream(extensionType, true);
	}

	/**
	 * Generate a stream for iterating over all registered extensions of the
	 * specified type.
	 *
	 * @param extensionType the type of {@link Extension} to stream
	 * @param reverse whether the extensions should be streamed in reversed
	 * registration order
	 */
	private <E extends Extension> Stream<E> stream(Class<E> extensionType, boolean reverse) {
		List<RegisteredExtension<E>> registeredExtensions = getRegisteredExtensions(extensionType);
		if (reverse) {
			Collections.reverse(registeredExtensions);
		}
		return registeredExtensions.stream().map(RegisteredExtension::getExtension);
	}

	/**
	 * Instantiate an extension of the given type using its default constructor
	 * and register it in this registry.
	 *
	 * <p>A new {@link Extension} will not be registered if an extension of the
	 * given type already exists in this registry.
	 *
	 * @param extensionType the type of extension to register
	 */
	void registerExtension(Class<? extends Extension> extensionType) {

		boolean extensionAlreadyRegistered = getRegisteredExtensionTypes().stream().anyMatch(
			registeredType -> registeredType.equals(extensionType));

		if (!extensionAlreadyRegistered) {
			registerExtension(ReflectionUtils.newInstance(extensionType));
			this.registeredExtensionTypes.add(extensionType);
		}
	}

	private void registerExtension(Extension extension) {
		registerExtension(extension, extension);
	}

	public void registerExtension(Extension extension, Object source) {
		LOG.finer(() -> String.format("Registering extension [%s] from source [%s].", extension, source));
		this.registeredExtensions.add(new RegisteredExtension<>(extension, source));
	}

}
