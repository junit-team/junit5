/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Default, mutable implementation of {@link ExtensionRegistry}.
 *
 * <p>A registry has a reference to its parent registry, and all lookups are
 * performed first in the current registry itself and then recursively in its
 * ancestors.
 *
 * @since 5.5
 */
@API(status = INTERNAL, since = "5.5")
public class MutableExtensionRegistry implements ExtensionRegistry, ExtensionRegistrar {

	private static final Logger logger = LoggerFactory.getLogger(MutableExtensionRegistry.class);

	private static final List<Extension> DEFAULT_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(//
		new DisabledCondition(), //
		new TempDirectory(), //
		new TimeoutExtension(), //
		new RepeatedTestExtension(), //
		new TestInfoParameterResolver(), //
		new TestReporterParameterResolver()));

	/**
	 * Factory for creating and populating a new root registry with the default
	 * extensions.
	 *
	 * <p>If the {@link org.junit.jupiter.engine.Constants#EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME}
	 * configuration parameter has been set to {@code true}, extensions will be
	 * auto-detected using Java's {@link ServiceLoader} mechanism and automatically
	 * registered after the default extensions.
	 *
	 * @param configuration configuration parameters used to retrieve the extension
	 * auto-detection flag; never {@code null}
	 * @return a new {@code ExtensionRegistry}; never {@code null}
	 */
	public static MutableExtensionRegistry createRegistryWithDefaultExtensions(JupiterConfiguration configuration) {
		MutableExtensionRegistry extensionRegistry = new MutableExtensionRegistry(null);

		// @formatter:off
		logger.trace(() -> "Registering default extensions: " + DEFAULT_EXTENSIONS.stream()
						.map(extension -> extension.getClass().getName())
						.collect(toList()));
		// @formatter:on

		DEFAULT_EXTENSIONS.forEach(extensionRegistry::registerDefaultExtension);

		if (configuration.isExtensionAutoDetectionEnabled()) {
			registerAutoDetectedExtensions(extensionRegistry);
		}

		return extensionRegistry;
	}

	private static void registerAutoDetectedExtensions(MutableExtensionRegistry extensionRegistry) {
		Iterable<Extension> extensions = ServiceLoader.load(Extension.class, ClassLoaderUtils.getDefaultClassLoader());

		// @formatter:off
		logger.config(() -> "Registering auto-detected extensions: "
				+ StreamSupport.stream(extensions.spliterator(), false)
						.map(extension -> extension.getClass().getName())
						.collect(toList()));
		// @formatter:on

		extensions.forEach(extensionRegistry::registerDefaultExtension);
	}

	/**
	 * Factory for creating and populating a new registry from a list of
	 * extension types and a parent registry.
	 *
	 * @param parentRegistry the parent registry
	 * @param extensionTypes the types of extensions to be registered in
	 * the new registry
	 * @return a new {@code ExtensionRegistry}; never {@code null}
	 */
	public static MutableExtensionRegistry createRegistryFrom(MutableExtensionRegistry parentRegistry,
			List<Class<? extends Extension>> extensionTypes) {

		Preconditions.notNull(parentRegistry, "parentRegistry must not be null");

		MutableExtensionRegistry registry = new MutableExtensionRegistry(parentRegistry);
		extensionTypes.forEach(registry::registerExtension);
		return registry;
	}

	private final MutableExtensionRegistry parent;

	private final Set<Class<? extends Extension>> registeredExtensionTypes = new LinkedHashSet<>();

	private final List<Extension> registeredExtensions = new ArrayList<>();

	private MutableExtensionRegistry(MutableExtensionRegistry parent) {
		this.parent = parent;
	}

	@Override
	public <E extends Extension> Stream<E> stream(Class<E> extensionType) {
		if (this.parent == null) {
			return streamLocal(extensionType);
		}
		return concat(this.parent.stream(extensionType), streamLocal(extensionType));
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

	@Override
	public void registerExtension(Extension extension, Object source) {
		Preconditions.notNull(extension, "Extension must not be null");
		Preconditions.notNull(source, "source must not be null");

		logger.trace(() -> String.format("Registering extension [%s] from source [%s].", extension, source));

		this.registeredExtensions.add(extension);
	}

}
