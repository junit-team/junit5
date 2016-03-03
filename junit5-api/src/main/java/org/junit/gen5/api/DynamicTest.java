/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import org.junit.gen5.commons.util.ExceptionUtils;

public class DynamicTest {

	private final String name;
	private final Executable executable;

	public DynamicTest(String name, Executable executable) {
		this.name = name;
		this.executable = executable;
	}

	public void execute() {
		try {
			executable.execute();
		}
		catch (Throwable throwable) {
			ExceptionUtils.throwAsUncheckedException(throwable);
		}
	}

	public String getName() {
		return name;
	}
}
