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

import org.junit.gen5.api.extension.Extension;
import org.junit.gen5.api.extension.ExtensionPointRegistry.Position;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * Represents an {@link Extension} registered in an {@link ExtensionRegistry}.
 *
 * @param <E> the type of registered {@link Extension}
 * @since 5.0
 */
@API(Internal)
public class RegisteredExtension<E extends Extension> implements Comparable<RegisteredExtension<E>> {

	private final E extension;

	private final Object source;

	private final Position position;

	/**
	 * Construct a new {@code RegisteredExtension} from the supplied
	 * extension, source, and position.
	 *
	 * <p>See {@link #getSource()} for an explanation of the semantics for
	 * the {@code source}.
	 *
	 * @param extension the physical {@code Extension} which is registered;
	 * never {@code null}
	 * @param source the <em>source</em> of the extension; used solely for
	 * error reporting and logging; never {@code null}
	 * @param position the position in which the extension is registered;
	 * never {@code null}
	 */
	public RegisteredExtension(E extension, Object source, Position position) {
		this.extension = Preconditions.notNull(extension, "extension must not be null");
		this.source = Preconditions.notNull(source, "source must not be null");
		this.position = Preconditions.notNull(position, "Position must not be null");
	}

	/**
	 * Get the physical implementation of the registered {@link Extension}.
	 */
	public E getExtension() {
		return this.extension;
	}

	/**
	 * Get the <em>source</em> of the registered {@link #getExtension Extension}.
	 *
	 * <p>The source is used solely for error reporting and logging.
	 *
	 * <h4>Semantics for Source</h4>
	 * <p>If an extension is registered declaratively via
	 * {@link org.junit.gen5.api.extension.ExtendWith @ExtendWith},
	 * {@link #getExtension()} this method will return the same
	 * object. However, if an extension is registered programmatically
	 * &mdash; for example, as a lambda expression or method reference by
	 * an {@link org.junit.gen5.api.extension.ExtensionRegistrar ExtensionRegistrar}
	 * or by the framework via the {@link ExtensionRegistry} &mdash;
	 * the {@code source} object may be the {@code ExtensionRegistrar} that
	 * registered the extension, the underlying
	 * {@link java.lang.reflect.Method} that implements the extension
	 * API, or similar.
	 */
	public Object getSource() {
		return this.source;
	}

	/**
	 * Get the position in which the {@link #getExtension Extension}
	 * is registered.
	 */
	public Position getPosition() {
		return this.position;
	}

	@Override
	public int compareTo(RegisteredExtension<E> that) {
		return Integer.compare(this.getPosition().ordinal(), that.getPosition().ordinal());
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("extension", this.extension)
				.append("source", this.source)
				.append("position", this.position)
				.toString();
		// @formatter:on
	}

}
