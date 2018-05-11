/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.extensions.TheoryParameterResolver;
import org.junit.jupiter.theories.extensions.TheoryTestFailureMessageFixer;
import org.junit.jupiter.theories.util.ArgumentUtils;

/**
 * The invocation context for a theory execution.
 */
@API(status = INTERNAL, since = "5.2")
public class TheoryInvocationContext implements TestTemplateInvocationContext {
	private final int permutationIndex;
	private final Map<Integer, DataPointDetails> theoryParameterArguments;
	private final TheoryDisplayNameFormatter displayNameFormatter;
	private final Method testMethod;

	private final TheoryParameterResolver theoryParameterResolver;
	private final TheoryTestFailureMessageFixer theoryTestFailureMessageFixer;

	/**
	 * Constructor.
	 *
	 * @param permutationIndex the (zero-based) index of this permutation
	 * @param theoryParameterArguments a map of parameter index to the
	 * corresponding argument
	 * @param displayNameFormatter the display name formatter
	 * @param testMethod the test method (theory) being executed
	 * @param argumentUtils utility class for working with arguments
	 */
	public TheoryInvocationContext(int permutationIndex, Map<Integer, DataPointDetails> theoryParameterArguments,
			TheoryDisplayNameFormatter displayNameFormatter, Method testMethod, ArgumentUtils argumentUtils) {

		this.permutationIndex = permutationIndex;
		this.theoryParameterArguments = Collections.unmodifiableMap(theoryParameterArguments);
		this.displayNameFormatter = displayNameFormatter;
		this.testMethod = testMethod;

		this.theoryParameterResolver = new TheoryParameterResolver(theoryParameterArguments);
		this.theoryTestFailureMessageFixer = new TheoryTestFailureMessageFixer(
			() -> argumentUtils.getArgumentsDescriptions(testMethod, theoryParameterArguments, "\n"));
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return Arrays.asList(theoryParameterResolver, theoryTestFailureMessageFixer);
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return displayNameFormatter.format(this);
	}

	/**
	 * @return the (zero-based) index of this permutation
	 */
	public int getPermutationIndex() {
		return permutationIndex;
	}

	/**
	 * @return a map of parameter index to the corresponding argument
	 */
	public Map<Integer, DataPointDetails> getTheoryParameterArguments() {
		return theoryParameterArguments;
	}

	/**
	 * @return the test method (theory) being executed
	 */
	public Method getTestMethod() {
		return testMethod;
	}
}
