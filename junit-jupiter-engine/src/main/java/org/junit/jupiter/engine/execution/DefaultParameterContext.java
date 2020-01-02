/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * @since 5.0
 */
class DefaultParameterContext implements ParameterContext {

	private final Parameter parameter;
	private final int index;
	private final Optional<Object> target;

	DefaultParameterContext(Parameter parameter, int index, Optional<Object> target) {
		Preconditions.condition(index >= 0, "index must be greater than or equal to zero");
		this.parameter = Preconditions.notNull(parameter, "parameter must not be null");
		this.index = index;
		this.target = Preconditions.notNull(target, "target must not be null");
	}

	@Override
	public Parameter getParameter() {
		return this.parameter;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public Optional<Object> getTarget() {
		return this.target;
	}

	@Override
	public boolean isAnnotated(Class<? extends Annotation> annotationType) {
		return AnnotationUtils.isAnnotated(getEffectiveAnnotatedParameter(), annotationType);
	}

	@Override
	public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType) {
		return AnnotationUtils.findAnnotation(getEffectiveAnnotatedParameter(), annotationType);
	}

	@Override
	public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType) {
		return AnnotationUtils.findRepeatableAnnotations(getEffectiveAnnotatedParameter(), annotationType);
	}

	/**
	 * Due to a bug in {@code javac} on JDK versions prior to JDK 9, looking up
	 * annotations directly on a {@link Parameter} will fail for inner class
	 * constructors.
	 *
	 * <h4>Bug in {@code javac} on JDK versions prior to JDK 9</h4>
	 *
	 * <p>The parameter annotations array in the compiled byte code for the user's
	 * test class excludes an entry for the implicit <em>enclosing instance</em>
	 * parameter for an inner class constructor.
	 *
	 * <h4>Workaround</h4>
	 *
	 * <p>JUnit provides a workaround for this off-by-one error by helping extension
	 * authors to access annotations on the preceding {@link Parameter} object (i.e.,
	 * {@code index - 1}). The {@linkplain #getIndex() current index} must never be
	 * zero in such situations since JUnit Jupiter should never ask a
	 * {@code ParameterResolver} to resolve a parameter for the implicit <em>enclosing
	 * instance</em> parameter.
	 *
	 * <h4>WARNING</h4>
	 *
	 * <p>The {@code AnnotatedElement} returned by this method should never be cast and
	 * treated as a {@code Parameter} since the metadata (e.g., {@link Parameter#getName()},
	 * {@link Parameter#getType()}, etc.) will not match those for the declared parameter
	 * at the given index in an inner class constructor.
	 *
	 * @return the actual {@code Parameter} for this context, or the <em>effective</em>
	 * {@code Parameter} if the aforementioned bug is detected
	 */
	private AnnotatedElement getEffectiveAnnotatedParameter() {
		Executable executable = getDeclaringExecutable();

		if (executable instanceof Constructor && isInnerClass(executable.getDeclaringClass())
				&& executable.getParameterAnnotations().length == executable.getParameterCount() - 1) {

			Preconditions.condition(this.index != 0,
				"A ParameterContext should never be created for parameter index 0 in an inner class constructor");

			return executable.getParameters()[this.index - 1];
		}

		return this.parameter;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("parameter", this.parameter)
				.append("index", this.index)
				.append("target", this.target)
				.toString();
		// @formatter:on
	}

}
