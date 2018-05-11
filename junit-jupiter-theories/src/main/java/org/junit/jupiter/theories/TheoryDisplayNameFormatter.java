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

import static java.util.stream.Collectors.joining;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.theories.Theory.CURRENT_PERMUTATION_PLACEHOLDER;
import static org.junit.jupiter.theories.Theory.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.theories.Theory.PARAMETER_DETAILS_PLACEHOLDER;
import static org.junit.jupiter.theories.Theory.PARAMETER_VALUES_PLACEHOLDER;
import static org.junit.jupiter.theories.Theory.PARAMETER_VALUES_WITH_INDEXES_PLACEHOLDER;
import static org.junit.jupiter.theories.Theory.TOTAL_PERMUTATIONS_PLACEHOLDER;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.domain.DataPointDetails;
import org.junit.jupiter.theories.util.ArgumentUtils;

/**
 * Formatter used to create the display name for theory executions.
 */
@API(status = INTERNAL, since = "5.2")
public class TheoryDisplayNameFormatter {
	private final String pattern;

	private final BiFunction<String, TheoryInvocationContext, String> patternModifier;

	/**
	 * Constructor.
	 *
	 * @param pattern the pattern for the display name
	 * @param displayName the display name provided by JUnit
	 * @param totalPermutations the total number of theory permutations to be
	 * performed
	 * @param argumentUtils utility class for working with arguments
	 */
	public TheoryDisplayNameFormatter(String pattern, String displayName, int totalPermutations,
			ArgumentUtils argumentUtils) {

		String modifiedPattern = pattern;
		modifiedPattern = modifiedPattern.replace(DISPLAY_NAME_PLACEHOLDER, displayName);
		modifiedPattern = modifiedPattern.replace(TOTAL_PERMUTATIONS_PLACEHOLDER, String.valueOf(totalPermutations));
		this.pattern = modifiedPattern;

		List<BiFunction<String, TheoryInvocationContext, String>> patternModifiers = new ArrayList<>();
		if (pattern.contains(CURRENT_PERMUTATION_PLACEHOLDER)) {
			patternModifiers.add(
				(v, ctx) -> v.replace(CURRENT_PERMUTATION_PLACEHOLDER, String.valueOf(ctx.getPermutationIndex() + 1)));
		}
		if (pattern.contains(PARAMETER_VALUES_PLACEHOLDER)) {
			patternModifiers.add((v, ctx) -> {
				String values = ctx.getTheoryParameterArguments().values().stream().map(DataPointDetails::getValue).map(
					String::valueOf).collect(joining(", "));
				return v.replace(PARAMETER_VALUES_PLACEHOLDER, values);
			});
		}
		if (pattern.contains(PARAMETER_VALUES_WITH_INDEXES_PLACEHOLDER)) {
			// @formatter:off
			patternModifiers.add((v, ctx) -> {
				String valuesWithIndexes = ctx.getTheoryParameterArguments().entrySet().stream()
						.map(entry -> entry.getValue().getValue() + " (index " + entry.getKey() + ")")
						.collect(joining(", "));
				return v.replace(PARAMETER_VALUES_WITH_INDEXES_PLACEHOLDER, valuesWithIndexes);
			});
			// @formatter:on
		}
		if (pattern.contains(PARAMETER_DETAILS_PLACEHOLDER)) {
			patternModifiers.add((v, ctx) -> v.replace(PARAMETER_DETAILS_PLACEHOLDER,
				argumentUtils.getArgumentsDescriptions(ctx.getTestMethod(), ctx.getTheoryParameterArguments(), ", ")));
		}

		//Very minor performance hit for using "identity-like" initial bi-function, but it greatly simplifies the code
		BiFunction<String, TheoryInvocationContext, String> collapsedPatternModifier = (v, ctx) -> v;
		for (BiFunction<String, TheoryInvocationContext, String> currModifier : patternModifiers) {
			collapsedPatternModifier = collapseModifiers(collapsedPatternModifier, currModifier);
		}
		this.patternModifier = collapsedPatternModifier;
	}

	/**
	 * Collapses two display name modifiers into a single {@link BiFunction}.
	 *
	 * @param firstModifier the first modifier
	 * @param secondModifier the second modifier
	 * @return the combined modifiers
	 */
	private BiFunction<String, TheoryInvocationContext, String> collapseModifiers(
			BiFunction<String, TheoryInvocationContext, String> firstModifier,
			BiFunction<String, TheoryInvocationContext, String> secondModifier) {
		return (v, ctx) -> secondModifier.apply(firstModifier.apply(v, ctx), ctx);
	}

	/**
	 * Creates the display name for the provided invocation context.
	 *
	 * @param context the context to create a display name for
	 * @return the constructed display name
	 */
	public String format(TheoryInvocationContext context) {
		return patternModifier.apply(pattern, context);
	}
}
