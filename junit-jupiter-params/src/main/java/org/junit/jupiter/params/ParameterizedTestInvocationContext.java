/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConverter;

/**
 * @since 5.0
 */
class ParameterizedTestInvocationContext implements TestTemplateInvocationContext {

	private final ParameterizedTestNameFormatter formatter;
	private final Object[] arguments;
	private final Map<Class<? extends ArgumentsAggregator>, ArgumentsAggregator> aggregators;
	private final Map<Class<? extends ArgumentConverter>, ArgumentConverter> converters;

	ParameterizedTestInvocationContext(ParameterizedTestNameFormatter formatter, Object[] arguments,
			Map<Class<? extends ArgumentsAggregator>, ArgumentsAggregator> aggregators,
			Map<Class<? extends ArgumentConverter>, ArgumentConverter> converters) {
		this.formatter = formatter;
		this.arguments = arguments;
		this.aggregators = aggregators;
		this.converters = converters;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(invocationIndex, this.arguments);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return singletonList(new ParameterizedTestParameterResolver(this.arguments, this.aggregators, this.converters));
	}

}
