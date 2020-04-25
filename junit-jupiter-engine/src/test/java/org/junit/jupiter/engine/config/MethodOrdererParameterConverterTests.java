/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_METHOD_ORDER_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.jupiter.engine.descriptor.CustomMethodOrderer;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.7
 */
@TrackLogRecords
class MethodOrdererParameterConverterTests {

	private static final String KEY = DEFAULT_TEST_METHOD_ORDER_PROPERTY_NAME;

	@Test
	void shouldReturnNoMethodOrderer(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(CustomMethodOrderer.class.getName()));

		MethodOrdererParameterConverter converter = new MethodOrdererParameterConverter();
		Optional<MethodOrderer> methodOrderer = converter.get(configurationParameters, KEY);

		assertThat(methodOrderer).isPresent().containsInstanceOf(MethodOrderer.class);
		assertExpectedLogMessage(listener, Level.INFO,
			"Using default method orderer 'org.junit.jupiter.engine.descriptor.CustomMethodOrderer' set via the "
					+ "'junit.jupiter.testmethod.order.default' configuration parameter.");
	}

	@Test
	void shouldReturnStandardMethodOrdererIfNoConfigurationFound() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.empty());

		MethodOrdererParameterConverter converter = new MethodOrdererParameterConverter();
		Optional<MethodOrderer> methodOrderer = converter.get(configurationParameters, KEY);

		assertThat(methodOrderer).isEmpty();
	}

	@Test
	void shouldReturnNoMethodOrdererIfConfigurationIsBlank() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(""));

		MethodOrdererParameterConverter converter = new MethodOrdererParameterConverter();
		Optional<MethodOrderer> methodOrderer = converter.get(configurationParameters, KEY);

		assertThat(methodOrderer).isEmpty();
	}

	@Test
	void shouldTrimAndReturnMethodOrderer(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		String classNameWithSpaces = " " + CustomMethodOrderer.class.getName() + "  ";
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(classNameWithSpaces));

		MethodOrdererParameterConverter converter = new MethodOrdererParameterConverter();
		Optional<MethodOrderer> methodOrderer = converter.get(configurationParameters, KEY);

		assertThat(methodOrderer).isPresent().containsInstanceOf(CustomMethodOrderer.class);
		assertExpectedLogMessage(listener, Level.INFO,
			"Using default method orderer 'org.junit.jupiter.engine.descriptor.CustomMethodOrderer' set via the "
					+ "'junit.jupiter.testmethod.order.default' configuration parameter.");
	}

	@Test
	void shouldReturnNoMethodOrdererIfNoClassFound(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of("random-string"));

		MethodOrdererParameterConverter converter = new MethodOrdererParameterConverter();
		Optional<MethodOrderer> methodOrderer = converter.get(configurationParameters, KEY);

		assertThat(methodOrderer).isEmpty();
		assertExpectedLogMessage(listener, Level.WARNING,
			"Failed to load default method orderer "
					+ "'random-string' set via the 'junit.jupiter.testmethod.order.default' "
					+ "configuration parameter. Falling back to default behavior.");
	}

	@Test
	void shouldReturnNoMethodOrdererIfClassFoundIsNotATypeOfMethodOrderer(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(Object.class.getName()));

		MethodOrdererParameterConverter converter = new MethodOrdererParameterConverter();
		Optional<MethodOrderer> methodOrderer = converter.get(configurationParameters, KEY);

		assertThat(methodOrderer).isEmpty();
		assertExpectedLogMessage(listener, Level.WARNING,
			"Failed to load default method orderer 'java.lang.Object' set via the 'junit.jupiter.testmethod.order.default' configuration parameter. "
					+ "Falling back to default behavior.");
	}

	@Test
	void shouldReturnNoMethodOrdererIfClassNameIsNotFullyQualified(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(CustomMethodOrderer.class.getSimpleName()));

		MethodOrdererParameterConverter converter = new MethodOrdererParameterConverter();
		Optional<MethodOrderer> methodOrderer = converter.get(configurationParameters, KEY);

		assertThat(methodOrderer).isEmpty();
		assertExpectedLogMessage(listener, Level.WARNING,
			"Failed to load default method orderer 'CustomMethodOrderer' "
					+ "set via the 'junit.jupiter.testmethod.order.default' configuration parameter. "
					+ "Falling back to default behavior.");
	}

	private void assertExpectedLogMessage(LogRecordListener listener, Level level, String expectedMessage) {
		// @formatter:off
		assertThat(listener.stream(level).map(LogRecord::getMessage)).contains(expectedMessage);
		// @formatter:on
	}

}
