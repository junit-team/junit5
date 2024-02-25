/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.Extension;

/**
 * An {@code ExtensionRegistrar} is used to register extensions.
 *
 * @since 5.5
 */
@API(status = INTERNAL, since = "5.5")
public interface ExtensionRegistrar {

	/**
	 * Instantiate an extension of the given type using its default constructor
	 * and register it in the registry.
	 *
	 * <p>A new {@link Extension} should not be registered if an extension of the
	 * given type already exists in the registry or a parent registry.
	 *
	 * @param extensionType the type of extension to register
	 * @since 5.8
	 */
	void registerExtension(Class<? extends Extension> extensionType);

	/**
	 * Register the supplied {@link Extension}, without checking if an extension
	 * of that type has already been registered.
	 *
	 * <h4>Semantics for Source</h4>
	 *
	 * <p>If an extension is registered <em>declaratively</em> via
	 * {@link org.junit.jupiter.api.extension.ExtendWith @ExtendWith}, the
	 * {@code source} and the {@code extension} should be the same object.
	 * However, if an extension is registered <em>programmatically</em> via
	 * {@link org.junit.jupiter.api.extension.RegisterExtension @RegisterExtension},
	 * the {@code source} object should be the {@link java.lang.reflect.Field}
	 * that is annotated with {@code @RegisterExtension}. Similarly, if an
	 * extension is registered <em>programmatically</em> as a lambda expression
	 * or method reference, the {@code source} object should be the underlying
	 * {@link java.lang.reflect.Method} that implements the extension API.
	 *
	 * @param extension the extension to register; never {@code null}
	 * @param source the source of the extension; never {@code null}
	 */
	void registerExtension(Extension extension, Object source);

	/**
	 * Register the supplied {@link Extension} as a <em>synthetic</em> extension,
	 * without checking if an extension of that type has already been registered.
	 *
	 * @param extension the extension to register; never {@code null}
	 * @param source the source of the extension; never {@code null}
	 * @since 5.8
	 * @see #registerExtension(Extension, Object)
	 */
	void registerSyntheticExtension(Extension extension, Object source);

}
