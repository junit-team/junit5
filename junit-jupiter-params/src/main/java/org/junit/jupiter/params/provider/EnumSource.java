/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.util.stream.Collectors.toSet;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code @EnumSource} is an {@link ArgumentsSource} for constants of
 * an {@link Enum}.
 *
 * <p>The enum constants will be provided as arguments to the annotated
 * {@code @ParameterizedTest} method.
 *
 * <p>The enum type can be specified explicitly using the {@link #value}
 * attribute. Otherwise, the declared type of the first parameter of the
 * {@code @ParameterizedTest} method is used.
 *
 * <p>The set of enum constants can be restricted via the {@link #names} and
 * {@link #mode} attributes.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.7")
@ArgumentsSource(EnumArgumentsProvider.class)
@SuppressWarnings("exports")
public @interface EnumSource {

	/**
	 * The enum type that serves as the source of the enum constants.
	 *
	 * <p>If this attribute is not set explicitly, the declared type of the
	 * first parameter of the {@code @ParameterizedTest} method is used.
	 *
	 * @see #names
	 * @see #mode
	 */
	Class<? extends Enum<?>> value() default NullEnum.class;

	/**
	 * The names of enum constants to provide, or regular expressions to select
	 * the names of enum constants to provide.
	 *
	 * <p>If no names or regular expressions are specified, all enum constants
	 * declared in the specified {@linkplain #value enum type} will be provided.
	 *
	 * <p>The {@link #mode} determines how the names are interpreted.
	 *
	 * @see #value
	 * @see #mode
	 */
	String[] names() default {};

	/**
	 * The enum constant selection mode.
	 *
	 * <p>Defaults to {@link Mode#INCLUDE INCLUDE}.
	 *
	 * @see Mode#INCLUDE
	 * @see Mode#EXCLUDE
	 * @see Mode#MATCH_ALL
	 * @see Mode#MATCH_ANY
	 * @see Mode#MATCH_NONE
	 * @see #names
	 */
	Mode mode() default Mode.INCLUDE;

	/**
	 * Enumeration of modes for selecting enum constants by name.
	 */
	enum Mode {

		/**
		 * Select only those enum constants whose names are supplied via the
		 * {@link EnumSource#names} attribute.
		 */
		INCLUDE(Mode::validateNames, (name, names) -> names.contains(name)),

		/**
		 * Select all declared enum constants except those supplied via the
		 * {@link EnumSource#names} attribute.
		 */
		EXCLUDE(Mode::validateNames, (name, names) -> !names.contains(name)),

		/**
		 * Select only those enum constants whose names match all patterns supplied
		 * via the {@link EnumSource#names} attribute.
		 *
		 * @see java.util.stream.Stream#allMatch(java.util.function.Predicate)
		 */
		MATCH_ALL(Mode::validatePatterns, (name, patterns) -> patterns.stream().allMatch(name::matches)),

		/**
		 * Select only those enum constants whose names match any pattern supplied
		 * via the {@link EnumSource#names} attribute.
		 *
		 * @see java.util.stream.Stream#anyMatch(java.util.function.Predicate)
		 */
		MATCH_ANY(Mode::validatePatterns, (name, patterns) -> patterns.stream().anyMatch(name::matches)),

		/**
		 * Select only those enum constants whose names match none of the patterns supplied
		 * via the {@link EnumSource#names} attribute.
		 *
		 * @since 5.9
		 * @see java.util.stream.Stream#noneMatch(java.util.function.Predicate)
		 */
		@API(status = EXPERIMENTAL, since = "5.9")
		MATCH_NONE(Mode::validatePatterns, (name, patterns) -> patterns.stream().noneMatch(name::matches));

		private final Validator validator;
		private final BiPredicate<String, Set<String>> selector;

		Mode(Validator validator, BiPredicate<String, Set<String>> selector) {
			this.validator = validator;
			this.selector = selector;
		}

		void validate(EnumSource enumSource, Set<? extends Enum<?>> constants, Set<String> names) {
			Preconditions.notNull(enumSource, "EnumSource must not be null");
			Preconditions.notNull(names, "names must not be null");

			validator.validate(enumSource, constants, names);
		}

		boolean select(Enum<?> constant, Set<String> names) {
			Preconditions.notNull(constant, "Enum constant must not be null");
			Preconditions.notNull(names, "names must not be null");

			return selector.test(constant.name(), names);
		}

		private static void validateNames(EnumSource enumSource, Set<? extends Enum<?>> constants, Set<String> names) {
			Set<String> allNames = constants.stream().map(Enum::name).collect(toSet());
			Preconditions.condition(allNames.containsAll(names),
				() -> "Invalid enum constant name(s) in " + enumSource + ". Valid names include: " + allNames);
		}

		private static void validatePatterns(EnumSource enumSource, Set<? extends Enum<?>> constants,
				Set<String> names) {
			try {
				names.forEach(Pattern::compile);
			}
			catch (PatternSyntaxException e) {
				throw new PreconditionViolationException(
					"Pattern compilation failed for a regular expression supplied in " + enumSource, e);
			}
		}

		private interface Validator {
			void validate(EnumSource enumSource, Set<? extends Enum<?>> constants, Set<String> names);
		}

	}

}
