/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;
import java.util.EnumSet;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

/**
 * Enumeration of Java Runtime Environment (JRE) versions.
 *
 * <p>If the current JRE version cannot be detected &mdash; for example, if the
 * {@code java.version} JVM system property is undefined &mdash; then none of
 * the constants defined in this enum will be considered to be the
 * {@linkplain #isCurrentVersion current JRE version}.
 *
 * @since 5.1
 * @see #JAVA_8
 * @see #JAVA_9
 * @see #JAVA_10
 * @see #JAVA_11
 * @see #JAVA_12
 * @see #JAVA_13
 * @see #JAVA_14
 * @see #JAVA_15
 * @see #JAVA_16
 * @see #JAVA_17
 * @see #JAVA_18
 * @see #JAVA_19
 * @see #JAVA_20
 * @see #JAVA_21
 * @see #JAVA_22
 * @see #OTHER
 * @see EnabledOnJre
 * @see DisabledOnJre
 * @see EnabledForJreRange
 * @see DisabledForJreRange
 */
@API(status = STABLE, since = "5.1")
public enum JRE {

	/**
	 * Java 8.
	 */
	JAVA_8,

	/**
	 * Java 9.
	 */
	JAVA_9,

	/**
	 * Java 10.
	 */
	JAVA_10,

	/**
	 * Java 11.
	 */
	JAVA_11,

	/**
	 * Java 12.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	JAVA_12,

	/**
	 * Java 13.
	 *
	 * @since 5.4
	 */
	@API(status = STABLE, since = "5.4")
	JAVA_13,

	/**
	 * Java 14.
	 *
	 * @since 5.5
	 */
	@API(status = STABLE, since = "5.5")
	JAVA_14,

	/**
	 * Java 15.
	 *
	 * @since 5.6
	 */
	@API(status = STABLE, since = "5.6")
	JAVA_15,

	/**
	 * Java 16.
	 *
	 * @since 5.7
	 */
	@API(status = STABLE, since = "5.7")
	JAVA_16,

	/**
	 * Java 17.
	 *
	 * @since 5.7.1
	 */
	@API(status = STABLE, since = "5.7.1")
	JAVA_17,

	/**
	 * Java 18.
	 *
	 * @since 5.8.1
	 */
	@API(status = STABLE, since = "5.8.1")
	JAVA_18,

	/**
	 * Java 19.
	 *
	 * @since 5.9
	 */
	@API(status = STABLE, since = "5.9")
	JAVA_19,

	/**
	 * Java 20.
	 *
	 * @since 5.9
	 */
	@API(status = STABLE, since = "5.9")
	JAVA_20,

	/**
	 * Java 21.
	 *
	 * @since 5.9.2
	 */
	@API(status = STABLE, since = "5.9.2")
	JAVA_21,

	/**
	 * Java 22.
	 *
	 * @since 5.10
	 */
	@API(status = STABLE, since = "5.10")
	JAVA_22,

	/**
	 * Java 23.
	 *
	 * @since 5.11
	 */
	@API(status = STABLE, since = "5.11")
	JAVA_23,

	/**
	 * A JRE version other than {@link #JAVA_8}, {@link #JAVA_9},
	 * {@link #JAVA_10}, {@link #JAVA_11}, {@link #JAVA_12},
	 * {@link #JAVA_13}, {@link #JAVA_14}, {@link #JAVA_15},
	 * {@link #JAVA_16}, {@link #JAVA_17}, {@link #JAVA_18},
	 * {@link #JAVA_19}, {@link #JAVA_20}, {@link #JAVA_21},
	 * {@link #JAVA_22}, or {@link #JAVA_23}.
	 */
	OTHER;

	private static final Logger logger = LoggerFactory.getLogger(JRE.class);

	private static final JRE CURRENT_VERSION = determineCurrentVersion();

	private static JRE determineCurrentVersion() {
		String javaVersion = System.getProperty("java.version");
		boolean javaVersionIsBlank = StringUtils.isBlank(javaVersion);

		if (javaVersionIsBlank) {
			logger.debug(
				() -> "JVM system property 'java.version' is undefined. It is therefore not possible to detect Java 8.");
		}

		if (!javaVersionIsBlank && javaVersion.startsWith("1.8")) {
			return JAVA_8;
		}

		try {
			// java.lang.Runtime.version() is a static method available on Java 9+
			// that returns an instance of java.lang.Runtime.Version which has the
			// following method: public int major()
			Method versionMethod = Runtime.class.getMethod("version");
			Object version = ReflectionUtils.invokeMethod(versionMethod, null);
			Method majorMethod = version.getClass().getMethod("major");
			int major = (int) ReflectionUtils.invokeMethod(majorMethod, version);
			switch (major) {
				case 9:
					return JAVA_9;
				case 10:
					return JAVA_10;
				case 11:
					return JAVA_11;
				case 12:
					return JAVA_12;
				case 13:
					return JAVA_13;
				case 14:
					return JAVA_14;
				case 15:
					return JAVA_15;
				case 16:
					return JAVA_16;
				case 17:
					return JAVA_17;
				case 18:
					return JAVA_18;
				case 19:
					return JAVA_19;
				case 20:
					return JAVA_20;
				case 21:
					return JAVA_21;
				case 22:
					return JAVA_22;
				case 23:
					return JAVA_23;
				default:
					return OTHER;
			}
		}
		catch (Exception ex) {
			logger.debug(ex, () -> "Failed to determine the current JRE version via java.lang.Runtime.Version.");
		}

		// null signals that the current JRE version is "unknown"
		return null;
	}

	/**
	 * @return {@code true} if <em>this</em> {@code JRE} is known to be the
	 * Java Runtime Environment version for the currently executing JVM or if
	 * the version is {@link #OTHER}
	 */
	public boolean isCurrentVersion() {
		return this == CURRENT_VERSION;
	}

	/**
	 * @return the {@link JRE} for the currently executing JVM, potentially
	 * {@link #OTHER}
	 *
	 * @since 5.7
	 */
	@API(status = STABLE, since = "5.7")
	public static JRE currentVersion() {
		return CURRENT_VERSION;
	}

	static boolean isCurrentVersionWithinRange(JRE min, JRE max) {
		return EnumSet.range(min, max).contains(CURRENT_VERSION);
	}

}
