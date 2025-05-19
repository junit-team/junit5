/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.Arguments.ArgumentSet;
import org.junit.platform.commons.util.Preconditions;

/**
 * Encapsulates the evaluation of an {@link Arguments} instance (so it happens
 * only once) and access to the resulting argument values.
 *
 * <p>The provided accessor methods are focused on the different use cases and
 * make it less error-prone to access the argument values.
 *
 * @since 5.13
 */
class EvaluatedArgumentSet {

	static EvaluatedArgumentSet allOf(Arguments arguments) {
		Object[] all = arguments.get();
		return create(all, all, arguments);
	}

	static EvaluatedArgumentSet of(Arguments arguments, IntUnaryOperator consumedLengthComputer) {
		Object[] all = arguments.get();
		Object[] consumed = dropSurplus(all, consumedLengthComputer.applyAsInt(all.length));
		return create(all, consumed, arguments);
	}

	private static EvaluatedArgumentSet create(Object[] all, Object[] consumed, Arguments arguments) {
		return new EvaluatedArgumentSet(all, consumed, determineName(arguments));
	}

	private final Object[] all;
	private final Object[] consumed;
	private final Optional<String> name;

	private EvaluatedArgumentSet(Object[] all, Object[] consumed, Optional<String> name) {
		this.all = all;
		this.consumed = consumed;
		this.name = name;
	}

	int getTotalLength() {
		return this.all.length;
	}

	Object[] getAllPayloads() {
		return extractFromNamed(this.all, Named::getPayload);
	}

	int getConsumedLength() {
		return this.consumed.length;
	}

	Object[] getConsumedNames() {
		return extractFromNamed(this.consumed, Named::getName);
	}

	Object[] getConsumedPayloads() {
		return extractFromNamed(this.consumed, Named::getPayload);
	}

	Object getConsumedPayload(int index) {
		return extractFromNamed(this.consumed[index], Named::getPayload);
	}

	Optional<String> getName() {
		return this.name;
	}

	private static Object[] dropSurplus(Object[] arguments, int newLength) {
		Preconditions.condition(newLength <= arguments.length,
			() -> "New length %d must be less than or equal to the total length %d".formatted(newLength,
				arguments.length));
		return arguments.length > newLength ? Arrays.copyOf(arguments, newLength) : arguments;
	}

	private static Optional<String> determineName(Arguments arguments) {
		if (arguments instanceof ArgumentSet) {
			return Optional.of(((ArgumentSet) arguments).getName());
		}
		return Optional.empty();
	}

	private static Object[] extractFromNamed(Object[] arguments, Function<Named<?>, Object> mapper) {
		return Arrays.stream(arguments) //
				.map(argument -> extractFromNamed(argument, mapper)) //
				.toArray();
	}

	private static Object extractFromNamed(Object argument, Function<Named<?>, Object> mapper) {
		return argument instanceof Named ? mapper.apply((Named<?>) argument) : argument;
	}

}
