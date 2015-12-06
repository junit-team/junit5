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
 * {@code TestLifeCycleExtension} defines the API for {@link TestExtension TestExtensions} that wish to provide
 * additional behavior to tests...
 * <p>
 * Implementers can be annotate with {@link TestExtension.DefaultOrder} to determine the order of extension application.
 * </p>
 * <p>
 * Implementations must provide a no-args constructor.
 *
 * @since 5.0
 */
public interface TestLifecycleExtension extends TestExtension {

	/**
	 * Evaluate this condition for the supplied {@link TestExtensionContext}.
	 * <p>
	 * An {@linkplain ConditionEvaluationResult#enabled enabled} result indicates that the test should be executed;
	 * whereas, a {@linkplain ConditionEvaluationResult#disabled disabled} result indicates that the test should not be
	 * executed.
	 *
	 * @param context the current {@code TestExtensionContext}
	 */
	default ConditionEvaluationResult shouldTestBeExecuted(TestExtensionContext context) {
		return ConditionEvaluationResult.enabled("");
	}

	/**
	 * Callback for post-processing or wrapping the test instance in the supplied
	 * {@link ExtensionContext}.
	 *
	 * @param testExtensionContext the current test execution context
	 */
	default Object postProcessTestInstance(TestExtensionContext testExtensionContext) throws Exception {
		return testExtensionContext.getTestInstance();
	}

	default void beforeEach(TestExtensionContext testExtensionContext) throws Exception {
		/* no-op */
	}

	default void executeTest(TestExtensionContext testExtensionContext, Executable testExecutable) throws Throwable {
		testExecutable.execute();
	}

	default void afterEach(TestExtensionContext testExtensionContext) throws Exception {
		/* no-op */
	}

}
