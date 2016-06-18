/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.timing;

// tag::user_guide[]
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestExtensionContext;

// end::user_guide[]
/**
 * Simple extension that <em>times</em> the execution of test methods and
 * logs the results at {@code INFO} level.
 *
 * @since 5.0
 */
// tag::user_guide[]
public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

	private static final Logger LOG = Logger.getLogger(TimingExtension.class.getName());

	@Override
	public void beforeTestExecution(TestExtensionContext context) throws Exception {
		getStore(context).put(context.getTestMethod().get(), System.currentTimeMillis());
	}

	@Override
	public void afterTestExecution(TestExtensionContext context) throws Exception {
		Method testMethod = context.getTestMethod().get();
		long start = (long) getStore(context).remove(testMethod);
		long duration = System.currentTimeMillis() - start;

		LOG.info(() -> String.format("Method [%s] took %s ms.", testMethod.getName(), duration));
	}

	private Store getStore(TestExtensionContext context) {
		return context.getStore(Namespace.of(getClass(), context));
	}

}
// end::user_guide[]
