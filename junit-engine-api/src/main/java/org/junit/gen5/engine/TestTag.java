/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.Serializable;
import java.util.Objects;

import org.junit.gen5.commons.meta.API;

/**
 * @since 5.0
 */
@API(Experimental)
public final class TestTag implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	public TestTag(String name) {
		this.name = name;
	}

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
