/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.ConfigurationParameters;

class DefaultJupiterConfigurationTests {

	private static final String KEY = DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;

	@Test
	void getDefaultTestInstanceLifecyclePreconditions() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> new DefaultJupiterConfiguration(null));
		assertThat(exception).hasMessage("ConfigurationParameters must not be null");
	}

	@Test
	void getDefaultTestInstanceLifecycleWithNoConfigParamSet() {
		JupiterConfiguration configuration = new DefaultJupiterConfiguration(mock(ConfigurationParameters.class));
		Lifecycle lifecycle = configuration.getDefaultTestInstanceLifecycle();
		assertThat(lifecycle).isEqualTo(PER_METHOD);
	}

	@Test
	void getDefaultTestInstanceLifecycleWithConfigParamSet() {
		assertAll(//
			() -> assertDefaultConfigParam(null, PER_METHOD), //
			() -> assertDefaultConfigParam("", PER_METHOD), //
			() -> assertDefaultConfigParam("bogus", PER_METHOD), //
			() -> assertDefaultConfigParam(PER_METHOD.name(), PER_METHOD), //
			() -> assertDefaultConfigParam(PER_METHOD.name().toLowerCase(), PER_METHOD), //
			() -> assertDefaultConfigParam("  " + PER_METHOD.name() + "  ", PER_METHOD), //
			() -> assertDefaultConfigParam(PER_CLASS.name(), PER_CLASS), //
			() -> assertDefaultConfigParam(PER_CLASS.name().toLowerCase(), PER_CLASS), //
			() -> assertDefaultConfigParam("  " + PER_CLASS.name() + "  ", Lifecycle.PER_CLASS) //
		);
	}

	private void assertDefaultConfigParam(String configValue, Lifecycle expected) {
		ConfigurationParameters configParams = mock(ConfigurationParameters.class);
		when(configParams.get(KEY)).thenReturn(Optional.ofNullable(configValue));
		Lifecycle lifecycle = new DefaultJupiterConfiguration(configParams).getDefaultTestInstanceLifecycle();
		assertThat(lifecycle).isEqualTo(expected);
	}

}
