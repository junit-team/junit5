/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;
import static org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils.getDefaultTestInstanceLifecycle;
import static org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils.getTestInstanceLifecycle;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * Unit tests for {@link TestInstanceLifecycleUtils}.
 *
 * <p>NOTE: it doesn't make sense to unit test the JVM system property fallback
 * support in this test class since that feature is a concrete implementation
 * detail of {@code LauncherConfigurationParameters} which necessitates an
 * integration test via the {@code Launcher} API.
 *
 * @since 5.0
 */
class TestInstanceLifecycleUtilsTests {

	private static final String KEY = DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;

	@Test
	void getDefaultTestInstanceLifecyclePreconditions() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> getDefaultTestInstanceLifecycle(null));
		assertThat(exception).hasMessage("ConfigurationParameters must not be null");
	}

	@Test
	void getDefaultTestInstanceLifecycleWithNoConfigParamSet() {
		Lifecycle lifecycle = getDefaultTestInstanceLifecycle(mock(ConfigurationParameters.class));
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
		Lifecycle lifecycle = getDefaultTestInstanceLifecycle(configParams);
		assertThat(lifecycle).isEqualTo(expected);
	}

	@Test
	void getTestInstanceLifecyclePreconditions() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> getTestInstanceLifecycle(null, mock(ConfigurationParameters.class)));
		assertThat(exception).hasMessage("testClass must not be null");

		exception = assertThrows(PreconditionViolationException.class,
			() -> getTestInstanceLifecycle(getClass(), null));
		assertThat(exception).hasMessage("ConfigurationParameters must not be null");
	}

	@Test
	void getTestInstanceLifecycleWithNoConfigParamSet() {
		Lifecycle lifecycle = getTestInstanceLifecycle(getClass(), mock(ConfigurationParameters.class));
		assertThat(lifecycle).isEqualTo(PER_METHOD);
	}

	@Test
	void getTestInstanceLifecycleWithConfigParamSet() {
		ConfigurationParameters configParams = mock(ConfigurationParameters.class);
		when(configParams.get(KEY)).thenReturn(Optional.of(PER_CLASS.name().toLowerCase()));
		Lifecycle lifecycle = getTestInstanceLifecycle(getClass(), configParams);
		assertThat(lifecycle).isEqualTo(PER_CLASS);
	}

	@Test
	void getTestInstanceLifecycleWithLocalConfigThatOverridesCustomDefaultSetViaConfigParam() {
		ConfigurationParameters configParams = mock(ConfigurationParameters.class);
		when(configParams.get(KEY)).thenReturn(Optional.of(PER_CLASS.name().toLowerCase()));
		Lifecycle lifecycle = getTestInstanceLifecycle(TestCase.class, configParams);
		assertThat(lifecycle).isEqualTo(PER_METHOD);
	}

	@Test
	void getTestInstanceLifecycleFromMetaAnnotationWithNoConfigParamSet() {
		Class<?> testClass = BaseMetaAnnotatedTestCase.class;
		Lifecycle lifecycle = getTestInstanceLifecycle(testClass, mock(ConfigurationParameters.class));
		assertThat(lifecycle).isEqualTo(PER_CLASS);
	}

	@Test
	void getTestInstanceLifecycleFromSpecializedClassWithNoConfigParamSet() {
		Class<?> testClass = SpecializedTestCase.class;
		Lifecycle lifecycle = getTestInstanceLifecycle(testClass, mock(ConfigurationParameters.class));
		assertThat(lifecycle).isEqualTo(PER_CLASS);
	}

	@TestInstance(Lifecycle.PER_METHOD)
	private static class TestCase {
	}

	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@TestInstance(Lifecycle.PER_CLASS)
	private @interface PerClassLifeCycle {
	}

	@PerClassLifeCycle
	private static class BaseMetaAnnotatedTestCase {
	}

	private static class SpecializedTestCase extends BaseMetaAnnotatedTestCase {
	}

}
