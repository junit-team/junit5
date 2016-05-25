/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * @since 5.0
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
