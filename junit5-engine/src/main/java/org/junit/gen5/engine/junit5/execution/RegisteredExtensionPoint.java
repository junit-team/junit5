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

import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPoint.Position;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * Represents an {@linkplain ExtensionPoint extension} registered in a
 * {@link TestExtensionRegistry}.
 *
 * @since 5.0
 * @param <T> the concrete subtype of {@link ExtensionPoint} to be registered
 */
public class RegisteredExtensionPoint<T extends ExtensionPoint> {

	private final T extensionPoint;

	private final Position position;

	private final String extensionName;

	public RegisteredExtensionPoint(T extensionPoint, Position position, String extensionName) {
		this.extensionPoint = extensionPoint;
		this.position = position;
		this.extensionName = extensionName;
	}

	public T getExtensionPoint() {
		return extensionPoint;
	}

	public Position getPosition() {
		return position;
	}

	public String getExtensionName() {
		return extensionName;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("position", this.position)
				.append("extensionName", this.extensionName)
				.toString();
		// @formatter:on
	}

}
