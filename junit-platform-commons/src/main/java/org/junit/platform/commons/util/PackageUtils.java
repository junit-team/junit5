/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.apiguardian.api.API;

/**
 * Collection of utilities for working with {@linkplain Package packages}.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public final class PackageUtils {

	private PackageUtils() {
		/* no-op */
	}

	static final String DEFAULT_PACKAGE_NAME = "";

	/**
	 * Compiled {@code "\."} pattern used to split canonical package (and type) names.
	 */
	private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

	/**
	 * Assert that the supplied package name is valid in terms of Java syntax.
	 *
	 * <p>Note: this method does not actually verify if the named package
	 * exists in the classpath.
	 *
	 * <p>The default package is represented by an empty string ({@code ""}).
	 *
	 * @param packageName the package name to validate
	 * @throws org.junit.platform.commons.PreconditionViolationException if the
	 * supplied package name is {@code null}, contains only whitespace, or
	 * contains parts that are not valid in terms of Java syntax (e.g.,
	 * containing keywords such as {@code void}, {@code import}, etc.)
	 * @see JavaNameUtils#isJavaName(String)
	 */
	public static void assertPackageNameIsValid(String packageName) {
		Preconditions.notNull(packageName, "package name must not be null");
		if (packageName.equals(DEFAULT_PACKAGE_NAME)) {
			return;
		}
		Preconditions.notBlank(packageName, "package name must not contain only whitespace");
		boolean allValid = Arrays.stream(DOT_PATTERN.split(packageName, -1)).allMatch(JavaNameUtils::isJavaName);
		Preconditions.condition(allValid, "invalid part(s) in package name: " + packageName);
	}

	/**
	 * Get the package attribute for the supplied {@code type} using the
	 * supplied {@code function}.
	 *
	 * <p>This method only returns a non-empty {@link Optional} value holder
	 * if the class loader for the supplied type created a {@link Package}
	 * object and the supplied function does not return {@code null} when
	 * applied.
	 *
	 * @param type the type to get the package attribute for
	 * @param function a function that computes the package attribute value
	 * (e.g., {@code Package::getImplementationTitle}); never {@code null}
	 * @return an {@code Optional} containing the attribute value; never
	 * {@code null} but potentially empty
	 * @throws org.junit.platform.commons.PreconditionViolationException if the
	 * supplied type or function is {@code null}
	 * @see Class#getPackage()
	 * @see Package#getImplementationTitle()
	 * @see Package#getImplementationVersion()
	 */
	public static Optional<String> getAttribute(Class<?> type, Function<Package, String> function) {
		Preconditions.notNull(type, "type must not be null");
		Preconditions.notNull(function, "function must not be null");
		Package typePackage = type.getPackage();
		if (typePackage != null) {
			return Optional.ofNullable(function.apply(typePackage));
		}
		return Optional.empty();
	}

	/**
	 * Get the value of the specified attribute name, specified as a string,
	 * or an empty {@link Optional} if the attribute was not found. The attribute
	 * name is case-insensitive.
	 *
	 * <p>This method also returns an empty {@link Optional} value holder
	 * if any exception is caught while loading the manifest file via the
	 * JAR file of the specified type.
	 *
	 * @param type the type to get the attribute for
	 * @param name the attribute name as a string
	 * @return an {@code Optional} containing the attribute value; never
	 * {@code null} but potentially empty
	 * @throws org.junit.platform.commons.PreconditionViolationException if the
	 * supplied type is {@code null} or the specified name is blank
	 * @see Manifest#getMainAttributes()
	 */
	public static Optional<String> getAttribute(Class<?> type, String name) {
		Preconditions.notNull(type, "type must not be null");
		Preconditions.notBlank(name, "name must not be blank");
		try {
			CodeSource codeSource = type.getProtectionDomain().getCodeSource();
			URL jarUrl = codeSource.getLocation();
			try (JarFile jarFile = new JarFile(new File(jarUrl.toURI()))) {
				Manifest manifest = jarFile.getManifest();
				Attributes mainAttributes = manifest.getMainAttributes();
				return Optional.ofNullable(mainAttributes.getValue(name));
			}
		}
		catch (Exception e) {
			return Optional.empty();
		}
	}

	/**
	 * Collection of utilities for working with qualified names in Java.
	 *
	 * @since 1.6
	 */
	static class JavaNameUtils {

		private static final Set<String> RESTRICTED_KEYWORDS = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList("_", "abstract", "assert", "break", "case", "catch", "const", "continue",
				"default", "do", "else", "enum", "false", "final", "finally", "for", "goto", "if", "import",
				"instanceof", "native", "new", "null", "private", "protected", "public", "return", "static", "strictfp",
				"super", "switch", "synchronized", "this", "throw", "transient", "true", "try", "volatile", "while")));

		/**
		 * Determine if the supplied {@code name} is a syntactically valid qualified name.
		 *
		 * @param name the string to check
		 * @return {@code true} if the supplied string is a syntactically valid name
		 */
		static boolean isJavaName(String name) {
			return name != null && !name.isEmpty() && isNotRestrictedKeyword(name) && isJavaIdentifier(name);
		}

		private static boolean isNotRestrictedKeyword(String name) {
			return !RESTRICTED_KEYWORDS.contains(name);
		}

		private static boolean isJavaIdentifier(String name) {
			int start = name.codePointAt(0);
			if (!Character.isJavaIdentifierStart(start)) {
				return false;
			}
			int charCount = Character.charCount(start);
			for (int i = charCount; i < name.length(); i += charCount) {
				int codePoint = name.codePointAt(i);
				if (!Character.isJavaIdentifierPart(codePoint)) {
					return false;
				}
			}
			return true;
		}
	}

}
