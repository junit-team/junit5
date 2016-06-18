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

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;

/**
 * Represents an {@link Extension} registered in an {@link ExtensionRegistry}.
 *
 * @param <E> the type of registered {@link Extension}
 * @since 5.0
 */
class RegisteredExtension<E extends Extension> {

	private final E extension;

	private final Object source;

	/**
	 * Construct a new {@code RegisteredExtension} from the supplied
	 * extension and source.
	 *
	 * <p>See {@link #getSource()} for an explanation of the semantics for
	 * the {@code source}.
	 *
	 * @param extension the physical {@code Extension} which is registered;
	 * never {@code null}
	 * @param source the <em>source</em> of the extension; used solely for
	 * error reporting and logging; never {@code null}
	 */
	RegisteredExtension(E extension, Object source) {
		this.extension = Preconditions.notNull(extension, "extension must not be null");
		this.source = Preconditions.notNull(source, "source must not be null");
	}

	/**
	 * Get the physical implementation of the registered {@link Extension}.
	 */
	E getExtension() {
		return this.extension;
	}

	/**
	 * Get the <em>source</em> of the registered {@link #getExtension Extension}.
	 *
	 * <p>The source is used solely for error reporting and logging.
	 *
	 * <h4>Semantics for Source</h4>
	 * <p>If an extension is registered declaratively via
	 * {@link ExtendWith @ExtendWith}, this method
	 * and {@link #getExtension()} will return the same object. However, if an
	 * extension is registered programmatically &mdash; for example, as a lambda
	 * expression or method reference &mdash; the {@code source} object may be the
	 * underlying {@link java.lang.reflect.Method} that implements the extension
	 * API, or similar.
	 */
	Object getSource() {
		return this.source;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
			.append("extension", this.extension)
			.append("source", this.source)
			.toString();
		// @formatter:on
	}

}
