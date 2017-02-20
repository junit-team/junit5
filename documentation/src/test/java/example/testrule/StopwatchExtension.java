/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.testrule;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExtensionContext;

/**
 * Simple extension that <em>times</em> the execution of test methods and emits
 * the results as a {@code ReportEntry}. This extension also allows then
 * insertion of the underlying Stopwatch as a test method parameter, so that
 * assertions can be made about the runtime.
 *
 * @since 5.0
 */
public class StopwatchExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {

	/**
	 * Creates a {@code Stopwatch} for this test and saves it in the
	 * {@code Store} for future duration calculations.
	 * 
	 * @see org.junit.jupiter.api.extension.BeforeTestExecutionCallback#beforeTestExecution(org.junit.jupiter.api.extension.TestExtensionContext)
	 */
	@Override
	public void beforeTestExecution(TestExtensionContext context) throws Exception {
		getStore(context).put(context.getTestMethod().get(), new Stopwatch());
	}

	/**
	 * When the test concludes, this method retrieves the {@Stopwatch} from the
	 * {@code Store} and emits a {@code ReportEntry} detailing the test's
	 * duration.
	 * 
	 * @see org.junit.jupiter.api.extension.AfterTestExecutionCallback#afterTestExecution(org.junit.jupiter.api.extension.TestExtensionContext)
	 */
	@Override
	public void afterTestExecution(TestExtensionContext context) throws Exception {
		Method testMethod = context.getTestMethod().get();
		Stopwatch stopwatch = getStore(context).remove(testMethod, Stopwatch.class);
		long duration = stopwatch.runtime(MICROSECONDS);

		context.publishReportEntry("Method execution time (in uS)", Long.toString(duration));
	}

	private Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(getClass(), context));
	}

	/**
	 * Indicates that this {@code ParameterResolver} will supply a
	 * {@code Stopwatch} initialized at the beginning of this test.
	 * 
	 * @see org.junit.jupiter.api.extension.ParameterResolver#supports(org.junit.jupiter.api.extension.ParameterContext,
	 *      org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return parameterContext.getParameter().getType().isAssignableFrom(Stopwatch.class);
	}

	/**
	 * Retrieves the {@code Stopwatch} associated with this test from the
	 * {@code Store} and returns it for use within the test body.
	 * 
	 * @see org.junit.jupiter.api.extension.ParameterResolver#resolve(org.junit.jupiter.api.extension.ParameterContext,
	 *      org.junit.jupiter.api.extension.ExtensionContext)
	 */
	@Override
	public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Method testMethod = extensionContext.getTestMethod().get();
		return getStore(extensionContext).get(testMethod, Stopwatch.class);
	}

}
