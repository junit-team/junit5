/*
 * Copyright 2015-2020 the original author or authors.
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
 * {@code @TempDir} can be used to annotate a non-private field in a test class
 * or a parameter in a lifecycle method or test method of type {@link Path} or
 * {@link File} that should be resolved into a temporary directory.
 *
 * <p>Please note that {@code @TempDir} is not supported on constructor
 * parameters. Please use field injection instead, by annotating a non-private
 * instance field with {@code @TempDir}.
 *
 * <h3>Temporary Directory Creation</h3>
 *
 * <p>The temporary directory is only created if a field in a test class or a
 * parameter in a lifecycle method or test method is annotated with
 * {@code @TempDir}. If the field type or parameter type is neither {@link Path}
 * nor {@link File} or if the temporary directory cannot be created, an
 * {@link ExtensionConfigurationException} or a
 * {@link ParameterResolutionException} will be thrown as appropriate. In
 * addition, a {@code ParameterResolutionException} will be thrown for a
 * constructor parameter annotated with {@code @TempDir}.
 *
 * <h3>Temporary Directory Scope</h3>
 *
 * <p>The scope of the temporary directory depends on where the first
 * {@code @TempDir} annotation is encountered when executing a test class. The
 * temporary directory will be shared by all tests in a class when the
 * annotation is present on a {@code static} field or on a parameter of a
 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method. Otherwise &mdash;
 * for example, when {@code @TempDir} is only used on instance fields or on
 * parameters in test, {@link org.junit.jupiter.api.BeforeEach @BeforeEach},
 * or {@link org.junit.jupiter.api.AfterEach @AfterEach} methods &mdash; each test
 * will use its own temporary directory.
 *
 * <h3>Temporary Directory Deletion</h3>
 *
 * <p>When the end of the scope of a temporary directory is reached, i.e. when
 * the test method or class has finished execution, JUnit will attempt to
 * recursively delete all files and directories in the temporary directory
 * and, finally, the temporary directory itself. In case deletion of a file or
 * directory fails, an {@link IOException} will be thrown that will cause the
 * test or test class to fail.
 *
 * @since 5.4
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "5.4")
public @interface TempDir {
}
