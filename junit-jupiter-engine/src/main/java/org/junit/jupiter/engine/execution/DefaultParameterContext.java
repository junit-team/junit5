/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.jupiter.api.extension.ParameterContext;
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
		return parameter;
	}

	@Override
	public int getIndex() {
		return index;
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
				.append("index", index)
				.append("target", target)
				.toString();
		// @formatter:on
	}
}
