/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.engine.Constants.Script.Reason.ANNOTATION_PLACEHOLDER;
import static org.junit.jupiter.engine.Constants.Script.Reason.RESULT_PLACEHOLDER;
import static org.junit.jupiter.engine.Constants.Script.Reason.SOURCE_PLACEHOLDER;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Script data class.
 *
 * @since 5.1
 * @see org.junit.jupiter.api.DisabledIf
 * @see org.junit.jupiter.api.EnabledIf
 * @see ScriptExecutionCondition
 * @see ScriptExecutionManager
 */
final class Script {

	private final Type annotationType;
	private final String annotationAsString;
	private final String engine;
	private final String source;
	private final String reason;
	private final int hashCode;

	Script(Annotation annotation, String engine, String source, String reason) {
		this(annotation.annotationType(), annotation.toString(), engine, source, reason);
	}

	Script(Type annotationType, String annotationAsString, String engine, String source, String reason) {
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

	String getAnnotationAsString() {
		return annotationAsString;
	}

	Type getAnnotationType() {
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
	String toReasonString(String resultAsString) {
		return reason.replace(ANNOTATION_PLACEHOLDER, getAnnotationAsString()) //
				.replace(SOURCE_PLACEHOLDER, getSource()) //
				.replace(RESULT_PLACEHOLDER, resultAsString);
	}

}
