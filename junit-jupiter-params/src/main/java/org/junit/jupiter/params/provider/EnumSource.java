/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code @EnumSource} is an {@link ArgumentsSource} for constants of a
 * specified {@linkplain #value Enum}.
 *
 * <p>The enum constants will be provided as arguments to the annotated
 * {@code @ParameterizedTest} method.
 *
 * <p>The set of enum constants can be restricted by listing the desired values
 * via the {@link #names} attribute.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Experimental)
@ArgumentsSource(EnumArgumentsProvider.class)
public @interface EnumSource {

	/**
	 * The enum type that serves as the source of the enum constants.
	 */
	Class<? extends Enum<?>> value();

	/**
	 * The names of enum constants to provide or regular expressions.
	 *
	 * <p>If no names are specified, all declared enum constants will be provided.
	*
	* <p>The {@link Mode} determines how the names are interpreted.
	 *
	* @see #mode()
	 */
	String[] names() default {};

	/**
	 * The enum constant selection mode.
	 *
	 * <p>Defaults to {@link Mode#INCLUDE_NAMES INCLUDE_NAMES}.
	 *
	 * @see #names()
	 */
	Mode mode() default Mode.INCLUDE_NAMES;

	/**
	 * The enum constant selection mode type definition.
	 */
	enum Mode {

		/**
		 * Select only those enum constants which name is listed in the {@linkplain #names} attribute.
		 */
		INCLUDE_NAMES(Mode::validateNames, (name, names) -> names.contains(name)),

		/**
		 * Select all declared enum constants except for those listed in the {@linkplain #names} attribute.
		 */
		EXCLUDE_NAMES(Mode::validateNames, (name, names) -> !names.contains(name)),

		/**
		 * Select only those enum constants which name matches all patterns listed by {@linkplain #names}.
		*
		* @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
		 */
		MATCHES_ALL(Mode::validatePatterns, (name, patterns) -> patterns.stream().allMatch(name::matches)),

		/**
		 * Select only those enum constants which name matches any pattern listed by {@linkplain #names}.
		 *
		 * @see java.util.stream.Stream#anyMatch(java.util.function.Predicate)
		 */
		MATCHES_ANY(Mode::validatePatterns, (name, patterns) -> patterns.stream().anyMatch(name::matches));

		final BiConsumer<EnumSource, Set<String>> validator;
		final BiPredicate<String, Set<String>> selector;

		Mode(BiConsumer<EnumSource, Set<String>> validator, BiPredicate<String, Set<String>> selector) {
			this.validator = validator;
			this.selector = selector;
		}

		void validate(EnumSource enumSource, Set<String> names) {
			Preconditions.notNull(enumSource, "enumSource must not be null");
			Preconditions.notNull(names, "names must not be null");

			validator.accept(enumSource, names);
		}

		boolean select(Enum<?> constant, Set<String> names) {
			Preconditions.notNull(constant, "constant must not be null");
			Preconditions.notNull(names, "names must not be null");

			return selector.test(constant.name(), names);
		}

		static void validateNames(EnumSource enumSource, Set<String> names) {
			Set<String> allNames = stream(enumSource.value().getEnumConstants()).map(e -> e.name()).collect(toSet());
			Preconditions.condition(allNames.containsAll(names),
				() -> "Invalid enum constant name(s) in: " + enumSource + ". Valid names are: " + allNames);
		}

		static void validatePatterns(EnumSource enumSource, Set<String> names) {
			try {
				names.forEach(Pattern::compile);
			}
			catch (PatternSyntaxException e) {
				throw new PreconditionViolationException(
					"Pattern compilation failed in: " + enumSource + " due to " + e, e);
			}
		}
	}

}
