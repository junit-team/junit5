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

import java.io.Serializable;

public class TestTag implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	public TestTag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
