/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.io;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * {@code @TempDir} can be used to annotate a field in a test class or a
 * parameter in a lifecycle method or test method of type {@link Path} or
 * {@link File} that should be resolved into a temporary directory.
 *
 * <p>Please note that {@code @TempDir} is not supported on constructor
 * parameters. Please use field injection instead by annotating an instance
 * field with {@code @TempDir}.
 *
 * <h3>Creation</h3>
 *
 * <p>The temporary directory is only created if a field in a test class or a
 * parameter in a lifecycle method or test method is annotated with
 * {@code @TempDir}. If the field type or parameter type is neither {@link Path}
 * nor {@link File}, if a field is declared as {@code final}, or if the temporary
 * directory cannot be created, an {@link ExtensionConfigurationException} or a
 * {@link ParameterResolutionException} will be thrown as appropriate. In
 * addition, a {@code ParameterResolutionException} will be thrown for a
 * constructor parameter annotated with {@code @TempDir}.
 *
 * <h3>Scope</h3>
 *
 * <p>By default, a separate temporary directory is created for every
 * declaration of the {@code @TempDir} annotation. If you want to share a
 * temporary directory across all tests in a test class, you should declare the
 * annotation on a {@code static} field or on a parameter of a
 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method.
 *
 * <h4>Old behavior</h4>
 *
 * <p>You can revert to the old behavior of using a single temporary directory
 * by setting the {@code junit.jupiter.tempdir.scope} configuration parameter to
 * {@code per_context}. In that case, the scope of the temporary directory
 * depends on where the first {@code @TempDir} annotation is encountered when
 * executing a test class. The temporary directory will be shared by all tests
 * in a class when the annotation is present on a {@code static} field or on a
 * parameter of a {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method.
 * Otherwise &mdash; for example, when {@code @TempDir} is only used on instance
 * fields or on parameters in test,
 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach}, or
 * {@link org.junit.jupiter.api.AfterEach @AfterEach} methods &mdash; each test
 * will use its own temporary directory.
 *
 * <h3>Clean Up</h3>
 *
 * <p>By default, when the end of the scope of a temporary directory is reached,
 * &mdash; when the test method or class has finished execution &mdash; JUnit will
 * attempt to clean up the temporary directory by recursively deleting all files
 * and directories in the temporary directory and, finally, the temporary directory
 * itself. In case deletion of a file or directory fails, an {@link IOException}
 * will be thrown that will cause the test or test class to fail.
 *
 * <p>The {@link #cleanup} attribute allows you to configure the {@link CleanupMode}.
 * If the cleanup mode is set to {@link CleanupMode#NEVER NEVER}, the temporary
 * directory will not be cleaned up after the test completes. If the cleanup mode is
 * set to {@link CleanupMode#ON_SUCCESS ON_SUCCESS}, the temporary directory will
 * only be cleaned up if the test completes successfully. By default, the
 * {@link CleanupMode#ALWAYS ALWAYS} clean up mode will be used, but this can be
 * configured globally by setting the {@value #DEFAULT_CLEANUP_MODE_PROPERTY_NAME}
 * configuration parameter.
 *
 * @since 5.4
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.4")
public @interface TempDir {

	/**
	 * The name of the configuration parameter that is used to configure the
	 * default {@link CleanupMode}.
	 *
	 * <p>If this configuration parameter is not set, {@link CleanupMode#ALWAYS}
	 * will be used as the default.
	 *
	 * @since 5.9
	 */
	@API(status = EXPERIMENTAL, since = "5.9")
	String DEFAULT_CLEANUP_MODE_PROPERTY_NAME = "junit.jupiter.tempdir.cleanup.mode.default";

	/**
	 * How the temporary directory gets cleaned up after the test completes.
	 *
	 * @since 5.9
	 */
	@API(status = EXPERIMENTAL, since = "5.9")
	CleanupMode cleanup() default CleanupMode.DEFAULT;

}
