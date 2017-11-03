/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Optional;

import org.apiguardian.api.API;

@FunctionalInterface
@API(status = STABLE, since = "5.1")
public interface AfterEngineExecutionCallback extends Extension {

	ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(AfterEngineExecutionCallback.class);

	default Optional<Class<?>> getTestInstanceClass() {
		return Optional.empty();
	}

	default void setTestInstance(Object testInstance) {
	}

	void afterEngineExecution(ExtensionContext context) throws Exception;

}
