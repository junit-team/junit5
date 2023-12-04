/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import org.apiguardian.api.API;
import org.junit.jupiter.api.AutoCloseUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * The {@code AutoCloseExtension} class is a JUnit 5 extension that automatically closes resources used in tests.
 *
 * <p>
 * This extension implements the {@link org.junit.jupiter.api.extension.AfterEachCallback} interface,
 * allowing it to perform resource cleanup after each test execution. It invokes the
 * {@link AutoCloseUtils#closeResources(Object)} method to close the resources annotated with
 * {@link org.junit.jupiter.api.AutoClose}.
 * </p>
 *
 * <p>
 * To use this extension, annotate your test class or test method with {@link org.junit.jupiter.api.extension.ExtendWith}
 * and provide an instance of {@code AutoCloseExtension}.
 * </p>
 *
 * @see org.junit.jupiter.api.extension.AfterEachCallback
 * @see org.junit.jupiter.api.extension.Extension
 * @see org.junit.jupiter.api.extension.ExtensionContext
 * @see org.junit.jupiter.api.AutoClose
 * @see AutoCloseUtils#closeResources(Object)
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.9")
public class AutoClose implements AfterEachCallback, Extension {

	/**
	 * Creates a new instance of AutoCloseExtension.
	 */
	public AutoClose() {

	}

	/**
	 * Invoked after each test execution to close the annotated resources within the test instance.
	 *
	 * @param context the extension context for the current test execution
	 * @throws Exception if an exception occurs during resource cleanup
	 */
	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		Object testInstance = context.getRequiredTestInstance();
		try {
			AutoCloseUtils.closeResources(testInstance);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
