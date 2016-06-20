/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.Serializable;
import java.util.Objects;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;

/**
 * Immutable value object for a <em>tag</em> that is assigned to a test or
 * container.
 *
 * @since 5.0
 */
@API(Experimental)
public final class TestTag implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	/**
	 * Obtain a {@code TestTag} with the supplied {@code name}.
	 *
	 * @param name the name of the tag; must not be null or blank
	 */
	public static TestTag of(String name) {
		return new TestTag(name);
	}

	private TestTag(String name) {
		Preconditions.notBlank(name, "name must not be null or blank");
		this.name = name;
	}

	/**
	 * Get the name of this tag.
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestTag) {
			TestTag that = (TestTag) obj;
			return Objects.equals(this.name, that.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

}
