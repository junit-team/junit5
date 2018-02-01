/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.script;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

import org.apiguardian.api.API;

/**
 * Script data class.
 *
 * @since 5.1
 * @see org.junit.jupiter.api.DisabledIf
 * @see org.junit.jupiter.api.EnabledIf
 * @see ScriptExecutionManager
 */
@API(status = EXPERIMENTAL, since = "5.1")
public final class Script {

	/**
	 * The script engine name defaults to {@code Nashorn}.
	 */
	public static final String DEFAULT_SCRIPT_ENGINE_NAME = "Nashorn";

	/**
	 * Set of all tags assigned to the current extension context.
	 *
	 * <p>Value type: {@code Set<String>}
	 *
	 * @see org.junit.jupiter.api.extension.ExtensionContext#getTags()
	 */
	public static final String BIND_JUNIT_TAGS = "junitTags";

	/**
	 * Unique ID associated with the current extension context.
	 *
	 * <p>Value type: {@code String}
	 *
	 * @see org.junit.jupiter.api.extension.ExtensionContext#getUniqueId()
	 */
	public static final String BIND_JUNIT_UNIQUE_ID = "junitUniqueId";

	/**
	 * Display name of the test or container.
	 *
	 * <p>Value type: {@code String}
	 *
	 * @see org.junit.jupiter.api.extension.ExtensionContext#getDisplayName()
	 */
	public static final String BIND_JUNIT_DISPLAY_NAME = "junitDisplayName";

	/**
	 * Accessor for JUnit Platform configuration parameters.
	 *
	 * <p>Usage: {@code junitConfigurationParameter.get(key) -> String}
	 *
	 * @see org.junit.jupiter.api.extension.ExtensionContext#getConfigurationParameter(String)
	 */
	public static final String BIND_JUNIT_CONFIGURATION_PARAMETER = "junitConfigurationParameter";

	/**
	 * Accessor for JVM system properties.
	 *
	 * <p>Usage: {@code systemProperty.get(key) -> String}
	 *
	 * @see System#getProperty(String)
	 */
	static final String BIND_SYSTEM_PROPERTY = "systemProperty";

	/**
	 * Accessor for operating system environment variables.
	 *
	 * <p>Usage: {@code systemEnvironment.get(key) -> String}
	 *
	 * @see System#getenv(String)
	 */
	static final String BIND_SYSTEM_ENVIRONMENT = "systemEnvironment";

	/**
	 * Default reason message pattern.
	 */
	public static final String DEFAULT_SCRIPT_REASON_PATTERN = "Script `{source}` evaluated to: {result}";

	/**
	 * Placeholder name for the {@code annotation.toString()} value.
	 */
	private static final String REASON_ANNOTATION_PLACEHOLDER = "{annotation}";

	/**
	 * Placeholder name for String representation of the result object.
	 */
	private static final String REASON_RESULT_PLACEHOLDER = "{result}";

	/**
	 * Placeholder name for the script source.
	 */
	private static final String REASON_SOURCE_PLACEHOLDER = "{source}";

	// ------------------------------------------------------------------------

	private final Type annotationType;
	private final String annotationAsString;
	private final String engine;
	private final String source;
	private final String reason;
	private final int hashCode;

	public Script(Annotation annotation, String engine, String source, String reason) {
		this(annotation.annotationType(), annotation.toString(), engine, source, reason);
	}

	public Script(Type annotationType, String annotationAsString, String engine, String source, String reason) {
		this.annotationType = annotationType;
		this.annotationAsString = annotationAsString;
		this.engine = engine;
		this.source = source;
		this.reason = reason;
		this.hashCode = computeHashCode();
	}

	/**
	 * Properties {@link #annotationType} and {@link #reason} are <b>not</b>
	 * included on purpose. This allows more cache hits when using instances
	 * of this class as keys in a hash map.
	 */
	private int computeHashCode() {
		return Objects.hash(annotationType.getTypeName(), engine, source);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (this.getClass() != other.getClass()) {
			return false;
		}
		Script otherScript = (Script) other;
		return this.hashCode == otherScript.hashCode;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public String getAnnotationAsString() {
		return annotationAsString;
	}

	public Type getAnnotationType() {
		return annotationType;
	}

	public String getEngine() {
		return engine;
	}

	public String getReason() {
		return reason;
	}

	public String getSource() {
		return source;
	}

	/**
	 * @return the string returned by {@link #getReason()} with all placeholders
	 * replaced with their current values stored here.
	 */
	public String toReasonString(String resultAsString) {
		return reason.replace(REASON_ANNOTATION_PLACEHOLDER, getAnnotationAsString()) //
				.replace(REASON_SOURCE_PLACEHOLDER, getSource()) //
				.replace(REASON_RESULT_PLACEHOLDER, resultAsString);
	}

}
