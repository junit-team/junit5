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
import static org.junit.jupiter.engine.Constants.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.CustomDisplayNameGenerator;
import org.junit.platform.engine.ConfigurationParameters;

class DisplayNameGeneratorClassParameterConverterTests {

	private static final String KEY = DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME;

	@Test
	void shouldReturnDefaultDisplayGeneratorClass() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(CustomDisplayNameGenerator.class.getName()));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isNotEmpty().hasValue(CustomDisplayNameGenerator.class);
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
	void shouldReturnEmptyIfNoConfigurationIsBlank() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(""));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
	}

	@Test
	void shouldTrimAndReturnDisplayNameClass() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		String classNameWithSpaces = " " + CustomDisplayNameGenerator.class.getName() + "  ";
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(classNameWithSpaces));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isNotEmpty().hasValue(CustomDisplayNameGenerator.class);
	}

	@Test
	void shouldReturnEmptyIfNoClassFound() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of("random-string"));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
	}

	@Test
	void shouldReturnEmptyIfClassFoundIsNotATypeOfDisplayNameGenerator() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(Optional.of(TestClass.class.getName()));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
	}

	@Test
	void shouldReturnEmptyIfClassNameIsNotFullyQualified() {
		ConfigurationParameters configurationParameters = mock(ConfigurationParameters.class);
		when(configurationParameters.get(KEY)).thenReturn(
			Optional.of(CustomDisplayNameGenerator.class.getSimpleName()));

		DisplayNameGeneratorClassParameterConverter converter = new DisplayNameGeneratorClassParameterConverter();

		Optional<Class<? extends DisplayNameGenerator>> displayNameClass = converter.get(configurationParameters, KEY);

		assertThat(displayNameClass).isEmpty();
	}

	static class TestClass {

	}

}
