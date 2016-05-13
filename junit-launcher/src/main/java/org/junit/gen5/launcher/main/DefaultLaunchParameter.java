/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.Objects;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ToStringBuilder;
import org.junit.gen5.launcher.LaunchParameter;

/**
 * @since 5.0
 */
@API(Experimental)
public class DefaultLaunchParameter implements LaunchParameter {
	private static final long serialVersionUID = 1L;

	public static LaunchParameter keyValuePair(String key, String value) {
		return new DefaultLaunchParameter(key, value);
	}

	private String key;
	private String value;

	private DefaultLaunchParameter(String key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		// @formatter:off
        return new ToStringBuilder(this)
                .append("key", key)
                .append("value", value)
                .toString();
        // @formatter:on
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DefaultLaunchParameter that = (DefaultLaunchParameter) o;
		return Objects.equals(key, that.key) && Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}
}
