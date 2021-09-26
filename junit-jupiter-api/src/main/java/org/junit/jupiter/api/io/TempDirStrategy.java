/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.io;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @TempDirStrategy} is a class-level annotation that is used to configure
 * the {@linkplain CleanupMode cleanup mode} of test instances for the annotated
 * test class or test interface.
 *
 * <p>If {@code @TempDirStrategy} is not explicitly declared on a test class or
 * on a test interface implemented by a test class, the cleanup mode will
 * implicitly default to {@link CleanupMode#ALWAYS}. Note, however,
 * that an explicit cleanup mode is <em>inherited</em> within a test class
 * hierarchy. In addition, the <em>default</em> cleanup mode may be overridden
 * via the {@code junit.jupiter.temp.dir.cleanup.mode.default} <em>configuration
 * parameter</em> which can be supplied via the {@code Launcher} API, build tools
 * (e.g., Gradle and Maven), a JVM system property, or the JUnit Platform
 * configuration file (i.e., a file named {@code junit-platform.properties} in
 * the root of the class path). Consult the User Guide for further information.
 *
 * <h3>Use Cases</h3>
 * <p>Setting the test instance cleanup mode to {@link CleanupMode#ALWAYS} will ensure that
 * temporary directories are always deleted after a test instance completes.
 * This is the default cleanup mode.
 * <p>Setting the test instance cleanup mode to {@link CleanupMode#NEVER} will ensure that
 * temporary directories are not deleted after a test instance completes.
 *
 * @since 5.9
 *
 * @see TempDir {@link TempDir}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@API(status = STABLE, since = "5.9")
public @interface TempDirStrategy {

	/**
	 * Enumeration of test instance <em>cleanup modes</em>.
	 *
	 * @see #ALWAYS
	 * @see #NEVER
	 */
	enum CleanupMode {

		/**
		 * When using this mode, a test directory will always be deleted when the class's tests complete.
		 *
		 * @see #NEVER
		 */
		ALWAYS,

		/**
		 * When using this mode, a test directory will not be deleted when the class's tests complete.
		 *
		 * @see #ALWAYS
		 */
		NEVER

	}

	/**
	 * The test directory <em>cleanup mode</em> to use.
	 */
	CleanupMode cleanupMode() default CleanupMode.ALWAYS;

}
