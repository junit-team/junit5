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

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.TestExtensionContext;

public abstract class LoggingExtension implements AfterTestExecutionCallback, BeforeTestExecutionCallback {

	public abstract String getPosition();

	@Override
	public void beforeTestExecution(TestExtensionContext context) throws Exception {
		context.publishReportEntry("before position", "starting " + getPosition() + " extension");
	}

	@Override
	public void afterTestExecution(TestExtensionContext context) throws Exception {
		context.publishReportEntry("after position", "finished " + getPosition() + " extension");
	}

}
