/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * An {@code ExtensionRegistry} holds all registered extensions (i.e.
 * instances of {@link Extension}) for a given
 * {@link org.junit.platform.engine.support.hierarchical.Node}.
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

	private static final List<Extension> DEFAULT_EXTENSIONS = Collections.unmodifiableList(
		Arrays.asList(new DisabledCondition(), new TestInfoParameterResolver(), new TestReporterParameterResolver()));

	/**
	 * Factory for creating and populating a new root registry with the default
	 * extensions.
	 *
	 * @return a new {@code ExtensionRegistry}
	 */
	public static ExtensionRegistry createRegistryWithDefaultExtensions() {
		ExtensionRegistry extensionRegistry = new ExtensionRegistry(null);
		DEFAULT_EXTENSIONS.forEach(extensionRegistry::registerDefaultExtension);
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

		ExtensionRegistry registry = new ExtensionRegistry(parentRegistry);
		extensionTypes.forEach(registry::registerExtension);
		return registry;
	}

	private final ExtensionRegistry parent;

	private final Set<Class<? extends Extension>> registeredExtensionTypes = new LinkedHashSet<>();

	private final List<Extension> registeredExtensions = new ArrayList<>();

	private ExtensionRegistry(ExtensionRegistry parent) {
		this.parent = parent;
	}

	/**
	 * Stream all {@code Extensions} of the specified type that are present
	 * in this registry or one of its ancestors.
	 *
	 * @param extensionType the type of {@link Extension} to stream
	 * @see #getReversedExtensions(Class)
	 * @see #getExtensions(Class)
	 */
	public <E extends Extension> Stream<E> stream(Class<E> extensionType) {
		if (this.parent == null) {
			return streamLocal(extensionType);
		}
		else {
			return concat(this.parent.stream(extensionType), streamLocal(extensionType));
		}
	}

	/**
	 * Stream all {@code Extensions} of the specified type that are present
	 * in this registry.
	 *
	 * <p>Extensions in ancestors are ignored.
	 *
	 * @param extensionType the type of {@link Extension} to stream
	 * @see #getReversedExtensions(Class)
	 */
	private <E extends Extension> Stream<E> streamLocal(Class<E> extensionType) {
		// @formatter:off
		return this.registeredExtensions.stream()
				.filter(extensionType::isInstance)
				.map(extensionType::cast);
		// @formatter:on
	}

	/**
	 * Get all {@code Extensions} of the specified type that are present
	 * in this registry or one of its ancestors.
	 *
	 * @param extensionType the type of {@link Extension} to get
	 * @see #getReversedExtensions(Class)
	 * @see #stream(Class)
	 */
	public <E extends Extension> List<E> getExtensions(Class<E> extensionType) {
		return stream(extensionType).collect(toList());
	}

	/**
	 * Get all {@code Extensions} of the specified type that are present
	 * in this registry or one of its ancestors, in reverse order.
	 *
	 * @param extensionType the type of {@link Extension} to get
	 * @see #getExtensions(Class)
	 * @see #stream(Class)
	 */
	public <E extends Extension> List<E> getReversedExtensions(Class<E> extensionType) {
		List<E> extensions = getExtensions(extensionType);
		Collections.reverse(extensions);
		return extensions;
	}

	/**
	 * Determine if the supplied type is already registered in this registry or in a
	 * parent registry.
	 */
	private boolean isAlreadyRegistered(Class<? extends Extension> extensionType) {
		return (this.registeredExtensionTypes.contains(extensionType)
				|| (this.parent != null && this.parent.isAlreadyRegistered(extensionType)));
	}

	/**
	 * Instantiate an extension of the given type using its default constructor
	 * and register it in this registry.
	 *
	 * <p>A new {@link Extension} will not be registered if an extension of the
	 * given type already exists in this registry or a parent registry.
	 *
	 * @param extensionType the type of extension to register
	 */
	void registerExtension(Class<? extends Extension> extensionType) {
		if (!isAlreadyRegistered(extensionType)) {
			registerExtension(ReflectionUtils.newInstance(extensionType));
			this.registeredExtensionTypes.add(extensionType);
		}
	}

	private void registerDefaultExtension(Extension extension) {
		this.registeredExtensions.add(extension);
		this.registeredExtensionTypes.add(extension.getClass());
	}

	private void registerExtension(Extension extension) {
		registerExtension(extension, extension);
	}

	/**
	 * Register the supplied {@link Extension} in this registry, without checking
	 * if an extension of that type already exists in this registry.
	 *
	 * <h4>Semantics for Source</h4>
	 * <p>If an extension is registered declaratively via {@link ExtendWith @ExtendWith},
	 * the {@code source} and the {@code extension} should be the same object. However,
	 * if an extension is registered programmatically &mdash; for example, as a lambda
	 * expression or method reference &mdash; the {@code source} object should be the
	 * underlying {@link java.lang.reflect.Method} that implements the extension
	 * API, or similar.
	 *
	 * @param extension the extension to register
	 * @param source the source of the extension
	 */
	public void registerExtension(Extension extension, Object source) {
		LOG.finer(() -> String.format("Registering extension [%s] from source [%s].", extension, source));
		this.registeredExtensions.add(extension);
	}

}
