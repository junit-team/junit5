/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package extensions;

import static org.junit.gen5.api.Assertions.assertNotNull;

import org.junit.gen5.api.extension.AfterEachCallback;
import org.junit.gen5.api.extension.ExtensionContext.Namespace;
import org.junit.gen5.api.extension.ExtensionContext.Store;
import org.junit.gen5.api.extension.TestExecutionExceptionHandler;
import org.junit.gen5.api.extension.TestExtensionContext;

public class ExpectToFailExtension implements TestExecutionExceptionHandler, AfterEachCallback {

	@Override
	public void handleTestExecutionException(TestExtensionContext context, Throwable throwable) throws Throwable {
		getExceptionStore(context).put("exception", throwable);
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		assertNotNull(getExceptionStore(context).get("exception"), "Test should have failed");
	}

	private Store getExceptionStore(TestExtensionContext context) {
		return context.getStore(Namespace.of(context));
	}
}
