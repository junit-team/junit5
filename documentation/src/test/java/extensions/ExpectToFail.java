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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExtensionContext;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ExpectToFail.Extension.class)
public @interface ExpectToFail {

	static class Extension implements TestExecutionExceptionHandler, AfterEachCallback {

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

}
