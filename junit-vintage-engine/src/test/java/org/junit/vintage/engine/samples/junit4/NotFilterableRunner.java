/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.samples.junit4;

import org.junit.runner.notification.RunNotifier;

/**
 * @since 5.1
 */
public class NotFilterableRunner extends ConfigurableRunner {

	public NotFilterableRunner(Class<?> testClass) {
		super(testClass);
	}

	@Override
	public void run(RunNotifier notifier) {
		getDescription().getChildren().forEach(child -> {
			notifier.fireTestStarted(child);
			notifier.fireTestFinished(child);
		});
	}

}
