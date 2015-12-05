/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import org.junit.gen5.api.Executable;

/**
 * {@code ContainerLifecycleExtension} defines the API for {@link TestExtension
 * TestExtensions} that wish to provide additional behavior to containers...
 *
 * <p>Implementers can annotate themselves or individual methods with {@linkplain org.junit.gen5.api.extension.TestExtension.Order}</p>
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 */
public interface ContainerLifecycleExtension extends TestExtension {

	/**
	 * Evaluate this condition for the supplied {@link TestExtensionContext}.
	 *
	 * <p>An {@linkplain ConditionEvaluationResult#enabled enabled} result indicates that all
	 * tests in this container should be executed; whereas, a {@linkplain ConditionEvaluationResult#disabled disabled}
	 * result indicates that the tests in the container should not be executed.
	 *
	 * @param containerExtensionContext the current {@code ContainerExtensionContext}
	 */
	default ConditionEvaluationResult shouldContainerBeExecuted(ContainerExtensionContext containerExtensionContext) {
		return ConditionEvaluationResult.enabled("");
	}

	default void beforeAll(ContainerExtensionContext containerExtensionContext) throws Exception {
		/* no-op */
	}

	default void executeContainer(ContainerExtensionContext containerExtensionContext, Executable containerExecutable)
			throws Throwable {
		containerExecutable.execute();
	}

	default void afterAll(ContainerExtensionContext containerExtensionContext) throws Exception {
		/* no-op */
	}

}
