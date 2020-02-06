/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Collections.singletonList;

import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

/**
 * {@code TestTemplateInvocationContext} for a {@link org.junit.jupiter.api.RepeatedTest @RepeatedTest}.
 *
 * @since 5.0
 */
class RepeatedTestInvocationContext implements TestTemplateInvocationContext {

	private final int currentRepetition;
	private final int totalRepetitions;
	private final RepeatedTestDisplayNameFormatter formatter;

	public RepeatedTestInvocationContext(int currentRepetition, int totalRepetitions,
			RepeatedTestDisplayNameFormatter formatter) {

		this.currentRepetition = currentRepetition;
		this.totalRepetitions = totalRepetitions;
		this.formatter = formatter;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(this.currentRepetition, this.totalRepetitions);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return singletonList(new RepetitionInfoParameterResolver(this.currentRepetition, this.totalRepetitions));
	}

}
