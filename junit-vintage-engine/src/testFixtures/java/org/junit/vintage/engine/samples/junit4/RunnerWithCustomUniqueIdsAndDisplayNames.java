/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.samples.junit4;

import static org.junit.runner.Description.createTestDescription;

import java.io.Serializable;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * @since 4.12
 */
public class RunnerWithCustomUniqueIdsAndDisplayNames extends BlockJUnit4ClassRunner {

	public RunnerWithCustomUniqueIdsAndDisplayNames(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected String getName() {
		DisplayName displayName = getTestClass().getAnnotation(DisplayName.class);
		return displayName == null ? super.getName() : displayName.value();
	}

	@Override
	protected Description describeChild(FrameworkMethod method) {
		String testName = getTestName(method);
		return createTestDescription(getTestClass().getJavaClass().getName(), testName, new CustomUniqueId(testName));
	}

	private String getTestName(FrameworkMethod method) {
		DisplayName displayName = method.getAnnotation(DisplayName.class);
		return displayName == null ? testName(method) : displayName.value();
	}

	private static class CustomUniqueId implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String testName;

		public CustomUniqueId(String testName) {
			this.testName = testName;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CustomUniqueId) {
				CustomUniqueId that = (CustomUniqueId) obj;
				return Objects.equals(this.testName, that.testName);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return testName.hashCode();
		}
	}
}
