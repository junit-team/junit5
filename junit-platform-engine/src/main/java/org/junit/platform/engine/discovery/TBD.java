/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import org.junit.platform.commons.util.Preconditions;

public class TBD {
	private final String prefix;
	private final String value;
	private final String fragment;

	private TBD(String prefix, String value, String fragment) {
		this.prefix = prefix;
		this.value = value;
		this.fragment = fragment;
	}

	public static TBD parse(String string) {
		Preconditions.notNull(string, "string must not be null");
		String[] parts = string.split(":", 2);
		Preconditions.condition(parts.length == 2, () -> "TBD string must be 'prefix:value', but was " + string);

		String[] valueParts = parts[1].split("#", 2);
		return new TBD(parts[0], valueParts[0], valueParts.length == 1 ? "" : valueParts[1]);
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
}
