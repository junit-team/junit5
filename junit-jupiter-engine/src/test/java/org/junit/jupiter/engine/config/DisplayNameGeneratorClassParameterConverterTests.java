/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.Constants.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.jupiter.engine.descriptor.CustomDisplayNameGenerator;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.engine.ConfigurationParameters;

@TrackLogRecords
class DisplayNameGeneratorClassParameterConverterTests {

	private static final String KEY = DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME;

	@Test
	void shouldReturnDefaultDisplayGeneratorClass(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(CustomDisplayNameGenerator.class.getName()));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isNotEmpty().hasValue(CustomDisplayNameGenerator.class);
		assertExpectedLogMessage(listener, Level.INFO,
			"Using default display name generator "
					+ "'org.junit.jupiter.engine.descriptor.CustomDisplayNameGenerator' set via the "
					+ "'junit.jupiter.displayname.generator.default' configuration parameter.");
	}

	@Test
	void shouldReturnEmptyIfNoConfigurationFound() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.empty());

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
	}

	@Test
	void shouldReturnEmptyIfNoConfigurationIsBlank(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(""));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
		assertExpectedLogMessage(listener, Level.WARNING,
			"No default display name generator class ''" + " set via the 'junit.jupiter.displayname.generator.default'"
					+ " configuration parameter is not not found. Falling back to default behaviour.");
	}

	@Test
	void shouldTrimAndReturnDisplayNameClass(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		String classNameWithSpaces = " " + CustomDisplayNameGenerator.class.getName() + "  ";
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(classNameWithSpaces));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isNotEmpty().hasValue(CustomDisplayNameGenerator.class);
		assertExpectedLogMessage(listener, Level.INFO,
			"Using default display name generator "
					+ "'org.junit.jupiter.engine.descriptor.CustomDisplayNameGenerator' set via the "
					+ "'junit.jupiter.displayname.generator.default' configuration parameter.");
	}

	@Test
	void shouldReturnEmptyIfNoClassFound(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of("random-string"));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
		assertExpectedLogMessage(listener, Level.WARNING,
			"No default display name generator class 'random-string' "
					+ "set via the 'junit.jupiter.displayname.generator.default' "
					+ "configuration parameter is not not found. Falling back to default behaviour.");
	}

	@Test
	void shouldReturnEmptyIfClassFoundIsNotATypeOfDisplayNameGenerator(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(TestClass.class.getName()));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
		assertExpectedLogMessage(listener, Level.WARNING, "Default display name generator class "
				+ "'org.junit.jupiter.engine.config.DisplayNameGeneratorClassParameterConverterTests$TestClass' "
				+ "set via the 'junit.jupiter.displayname.generator.default' configuration parameter is not of type "
				+ "`org.junit.jupiter.api.DisplayNameGenerator`.Falling back to default behaviour.");
	}

	@Test
	void shouldReturnEmptyIfClassNameIsNotFullyQualified(LogRecordListener listener) {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(
			Optional.of(CustomDisplayNameGenerator.class.getSimpleName()));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
		assertExpectedLogMessage(listener, Level.WARNING,
			"No default display name generator class "
					+ "'CustomDisplayNameGenerator' set via the 'junit.jupiter.displayname.generator.default' "
					+ "configuration parameter is not not found. Falling back to default behaviour.");
	}

	private void assertExpectedLogMessage(LogRecordListener listener, Level level, String expectedMessage) {
		// @formatter:off
        assertTrue(listener.stream(level)
                .map(LogRecord::getMessage)
                .anyMatch(expectedMessage::equals));
        // @formatter:on
	}

	static class TestClass {

	}

}
