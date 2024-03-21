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

import org.junit.platform.commons.util.Preconditions;

public final class DiscoverySelectorIdentifier {

	private final String prefix;
	private final String value;
	private final String fragment;

	public static DiscoverySelectorIdentifier create(String prefix, String value) {
		return create(prefix, value, "");
	}

	public static DiscoverySelectorIdentifier create(String prefix, String value, String fragment) {
		return new DiscoverySelectorIdentifier(prefix, value, fragment);
	}

	public static DiscoverySelectorIdentifier parse(String string) {
		Preconditions.notNull(string, "string must not be null");
		String[] parts = string.split(":", 2);
		Preconditions.condition(parts.length == 2, () -> "Identifier string must be 'prefix:value', but was " + string);

		String[] valueParts = parts[1].split("#", 2);
		return new DiscoverySelectorIdentifier(parts[0], valueParts[0], valueParts.length == 1 ? "" : valueParts[1]);
	}

	private DiscoverySelectorIdentifier(String prefix, String value, String fragment) {
		this.prefix = prefix;
		this.value = value;
		this.fragment = fragment;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getValue() {
		return value;
	}

	public String getFragment() {
		return fragment;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DiscoverySelectorIdentifier that = (DiscoverySelectorIdentifier) o;
		return Objects.equals(prefix, that.prefix) && Objects.equals(value, that.value)
				&& Objects.equals(fragment, that.fragment);
	}

	@Override
	public int hashCode() {
		return Objects.hash(prefix, value, fragment);
	}

	@Override
	public String toString() {
		return String.format("%s:%s%s", prefix, value, fragment.isEmpty() ? "" : "#" + fragment);
	}
}
