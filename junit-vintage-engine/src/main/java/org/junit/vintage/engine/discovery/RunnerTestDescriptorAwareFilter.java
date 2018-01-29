/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;

/**
 * @since 4.12
 */
abstract class RunnerTestDescriptorAwareFilter extends Filter {

	abstract void initialize(RunnerTestDescriptor runnerTestDescriptor);

	static RunnerTestDescriptorAwareFilter adapter(Filter filter) {
		return new RunnerTestDescriptorAwareFilter() {
			@Override
			void initialize(RunnerTestDescriptor runnerTestDescriptor) {
				// do nothing
			}

			@Override
			public boolean shouldRun(Description description) {
				return filter.shouldRun(description);
			}

			@Override
			public String describe() {
				return filter.describe();
			}
		};
	}

}
