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

import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPoint.Position;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * Represents an {@linkplain ExtensionPoint extension} registered in a
 * {@link TestExtensionRegistry}.
 *
 * @param <E> the type of registered {@link ExtensionPoint}
 * @since 5.0
 */
public class RegisteredExtensionPoint<E extends ExtensionPoint> {

	private final E extensionPoint;

	private final Position position;

	public RegisteredExtensionPoint(E extensionPoint, Position position) {
		this.extensionPoint = extensionPoint;
		this.position = position;
	}

	public E getExtensionPoint() {
		return this.extensionPoint;
	}

	public Position getPosition() {
		return this.position;
	}

	public String getExtensionName() {
		return this.extensionPoint.getClass().getName();
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("position", this.position)
				.append("extensionPoint", this.extensionPoint)
				.toString();
		// @formatter:on
	}

}
