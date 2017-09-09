/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.runner.Request;
import org.junit.runner.Runner;

/**
 * @since 4.12
 */
@API(status = INTERNAL, since = "4.12")
public class RunnerTestDescriptor extends VintageTestDescriptor {

	private final Runner runner;

	public RunnerTestDescriptor(UniqueId uniqueId, Class<?> testClass, Runner runner) {
		super(uniqueId, runner.getDescription(), testClass.getName(), ClassSource.from(testClass));
		this.runner = runner;
	}

	public Request toRequest() {
		return new RunnerRequest(this.runner);
	}

}
