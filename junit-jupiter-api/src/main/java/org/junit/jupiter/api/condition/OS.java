/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.Locale;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.StringUtils;

/**
 * Enumeration of common operating systems used for testing Java applications.
 *
 * @since 5.1
 * @see EnabledOnOs
 * @see DisabledOnOs
 */
@API(status = STABLE, since = "5.1")
public enum OS {

	/**
	 * Linux-based operating system.
	 */
	LINUX,

	/**
	 * Apple Macintosh operating system (e.g., macOS).
	 */
	MAC,

	/**
	 * Oracle Solaris operating system.
	 */
	SOLARIS,

	/**
	 * Microsoft Windows operating system.
	 */
	WINDOWS,

	/**
	 * An operating system other than {@link #LINUX}, {@link #MAC},
	 * {@link #SOLARIS}, or {@link #WINDOWS}.
	 *
	 * <p>Note that {@code OTHER} will be considered to be the {@linkplain
	 * #isCurrentOs current operating system} if the current operating system
	 * could not be detected &mdash; for example, if the {@code os.name} JVM
	 * system property is undefined.
	 */
	OTHER;

	private static final Logger logger = LoggerFactory.getLogger(OS.class);

	private static final OS CURRENT_OS = determineCurrentfOs();

	private static OS determineCurrentfOs() {
		String osName = System.getProperty("os.name");

		if (StringUtils.isBlank(osName)) {
			logger.debug(
				() -> "JVM system property 'os.name' is undefined. It is therefore not possible to detect the current OS.");
			return OTHER;
		}

		osName = osName.toLowerCase(Locale.ENGLISH);

		if (osName.contains("linux")) {
			return LINUX;
		}
		if (osName.contains("mac")) {
			return MAC;
		}
		if (osName.contains("solaris")) {
			return SOLARIS;
		}
		if (osName.contains("win")) {
			return WINDOWS;
		}
		return OTHER;
	}

	/**
	 * @return {@code true} if <em>this</em> {@code OS} is the operating system
	 * on which the current JVM is executing
	 */
	public boolean isCurrentOs() {
		return this == CURRENT_OS;
	}

}
