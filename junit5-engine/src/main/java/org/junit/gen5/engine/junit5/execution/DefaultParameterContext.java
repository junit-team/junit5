/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.gen5.api.extension.ParameterContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;

class DefaultParameterContext implements ParameterContext {

	private final Parameter parameter;
	private final Optional<Object> target;

	DefaultParameterContext(Parameter parameter, Optional<Object> target) {
		this.parameter = Preconditions.notNull(parameter, "parameter must not be null");
		this.target = Preconditions.notNull(target, "target must not be null");
	}

	@Override
	public Parameter getParameter() {
		return parameter;
	}

	@Override
	public Optional<Object> getTarget() {
		return target;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("parameter", parameter)
				.append("target", target)
				.toString();
		// @formatter:on
	}
}
