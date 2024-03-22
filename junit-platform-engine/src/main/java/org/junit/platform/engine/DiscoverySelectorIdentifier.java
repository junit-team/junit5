/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import java.util.Objects;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.StringUtils;

public final class DiscoverySelectorIdentifier {

	private final String prefix;
	private final String value;

	public static DiscoverySelectorIdentifier create(String prefix, String value) {
		return new DiscoverySelectorIdentifier(prefix, value);
	}

	public static DiscoverySelectorIdentifier parse(String string) {
		return StringUtils.splitIntoTwo(':', string).mapTwo( //
			() -> new PreconditionViolationException("Identifier string must be 'prefix:value', but was " + string), //
			DiscoverySelectorIdentifier::new //
		);
	}

	private DiscoverySelectorIdentifier(String prefix, String value) {
		this.prefix = prefix;
		this.value = value;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		DiscoverySelectorIdentifier that = (DiscoverySelectorIdentifier) o;
		return Objects.equals(prefix, that.prefix) && Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(prefix, value);
	}

	@Override
	public String toString() {
		return String.format("%s:%s", prefix, value);
	}
}
