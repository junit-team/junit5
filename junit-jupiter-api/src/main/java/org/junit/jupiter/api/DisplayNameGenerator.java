/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.lang.reflect.Method;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code DisplayNameGenerator} defines the SPI for generating display names
 * programmatically.
 *
 * <p>Display names are typically used for test reporting in IDEs and build
 * tools and may contain spaces, special characters, and even emoji.
 *
 * <p>Concrete implementations must have a <em>default constructor</em>.
 *
 * @since 5.4
 * @see DisplayName @DisplayName
 * @see DisplayNameGeneration @DisplayNameGeneration
 */
@API(status = STABLE, since = "5.7")
public interface DisplayNameGenerator {

	/**
	 * Generate a display name for the given top-level or {@code static} nested test class.
	 *
	 * @param testClass the class to generate a name for; never {@code null}
	 * @return the display name for the class; never {@code null} or blank
	 */
	String generateDisplayNameForClass(Class<?> testClass);

	/**
	 * Generate a display name for the given {@link Nested @Nested} inner test class.
	 *
	 * @param nestedClass the class to generate a name for; never {@code null}
	 * @return the display name for the nested class; never {@code null} or blank
	 */
	String generateDisplayNameForNestedClass(Class<?> nestedClass);

	/**
	 * Generate a display name for the given method.
	 *
	 * @implNote The class instance supplied as {@code testClass} may differ from
	 * the class returned by {@code testMethod.getDeclaringClass()} &mdash; for
	 * example, when a test method is inherited from a superclass.
	 *
	 * @param testClass the class the test method is invoked on; never {@code null}
	 * @param testMethod method to generate a display name for; never {@code null}
	 * @return the display name for the test; never {@code null} or blank
	 */
	String generateDisplayNameForMethod(Class<?> testClass, Method testMethod);

	/**
	 * Generate a string representation of the formal parameters of the supplied
	 * method, consisting of the {@linkplain Class#getSimpleName() simple names}
	 * of the parameter types, separated by commas, and enclosed in parentheses.
	 *
	 * @param method the method from to extract the parameter types from; never
	 * {@code null}
	 * @return a string representation of all parameter types of the supplied
	 * method or {@code "()"} if the method declares no parameters
	 */
	static String parameterTypesAsString(Method method) {
		Preconditions.notNull(method, "Method must not be null");
		return '(' + ClassUtils.nullSafeToString(Class::getSimpleName, method.getParameterTypes()) + ')';
	}

	/**
	 * Standard {@code DisplayNameGenerator}.
	 *
	 * <p>This implementation matches the standard display name generation
	 * behavior in place since JUnit Jupiter 5.0 was released.
	 */
	class Standard implements DisplayNameGenerator {

		static final DisplayNameGenerator INSTANCE = new Standard();

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			String name = testClass.getName();
			int lastDot = name.lastIndexOf('.');
			return name.substring(lastDot + 1);
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return nestedClass.getSimpleName();
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			return testMethod.getName() + parameterTypesAsString(testMethod);
		}
	}

	/**
	 * Simple {@code DisplayNameGenerator} that removes trailing parentheses
	 * for methods with no parameters.
	 *
	 * <p>This generator extends the functionality of {@link Standard} by
	 * removing parentheses ({@code '()'}) found at the end of method names
	 * with no parameters.
	 */
	class Simple extends Standard {

		static final DisplayNameGenerator INSTANCE = new Simple();

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			String displayName = testMethod.getName();
			if (hasParameters(testMethod)) {
				displayName += ' ' + parameterTypesAsString(testMethod);
			}
			return displayName;
		}

		private static boolean hasParameters(Method method) {
			return method.getParameterCount() > 0;
		}

	}

	/**
	 * {@code DisplayNameGenerator} that replaces underscores with spaces.
	 *
	 * <p>This generator extends the functionality of {@link Simple} by
	 * replacing all underscores ({@code '_'}) found in class and method names
	 * with spaces ({@code ' '}).
	 */
	class ReplaceUnderscores extends Simple {

		static final DisplayNameGenerator INSTANCE = new ReplaceUnderscores();

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			return replaceUnderscores(super.generateDisplayNameForClass(testClass));
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return replaceUnderscores(super.generateDisplayNameForNestedClass(nestedClass));
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			return replaceUnderscores(super.generateDisplayNameForMethod(testClass, testMethod));
		}

		private static String replaceUnderscores(String name) {
			return name.replace('_', ' ');
		}

	}

	/**
	 * {@code DisplayNameGenerator} that generates complete sentences.
	 *
	 * <p>This implements the functionality of {@link DisplayNameGenerator}
	 * by generating complete sentences display names, these
	 * sentences are divided with a separator, and the generator and separator
	 * can be customisable by using the {@link IndicativeSentencesGeneration}
	 * interface as annotation.
	 *
	 * @since 5.7
	 */
	@API(status = EXPERIMENTAL, since = "5.7")
	class IndicativeSentences implements DisplayNameGenerator {

		static final DisplayNameGenerator INSTANCE = new IndicativeSentences();

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			return getGeneratorForIndicativeSentence(testClass).generateDisplayNameForClass(testClass);
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return getSentenceBeginning(nestedClass);
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			return getSentenceBeginning(testClass) + getSentenceSeparator(testClass)
					+ getGeneratorForIndicativeSentence(testClass).generateDisplayNameForMethod(testClass, testMethod);
		}

		private String getSentenceBeginning(Class<?> testClass) {
			Class<?> enclosingParent = testClass.getEnclosingClass();
			Optional<DisplayName> displayName = findAnnotation(testClass, DisplayName.class);
			Optional<DisplayNameGeneration> displayNameGeneration = findAnnotation(testClass,
				DisplayNameGeneration.class);

			if (enclosingParent == null || displayNameGeneration.isPresent()) {
				return displayName.map(DisplayName::value).orElseGet(() -> generateDisplayNameForClass(testClass));
			}
			String separator = getSentenceSeparator(testClass);
			String sentenceBeginning = getSentenceBeginning(enclosingParent);
			return displayName.map(name -> sentenceBeginning + separator + name.value()) //
					.orElseGet(() -> sentenceBeginning + separator
							+ getGeneratorForIndicativeSentence(testClass).generateDisplayNameForNestedClass(
								testClass));
		}

		/**
		 * Gets the separator for {@link IndicativeSentencesGeneration} when extracting the
		 * annotation from {@code IndicativeSentencesGeneration}, if it doesn't find it,
		 * then search for the parent classes, if no separator is found use @code{", "} by default.
		 *
		 * @param testClass Class to get Indicative sentence annotation separator either custom or default
		 * @return the indicative sentence separator
		 */
		private String getSentenceSeparator(Class<?> testClass) {
			Optional<IndicativeSentencesGeneration> indicativeSentencesGeneration = getIndicativeSentencesGeneration(
				testClass);
			if (indicativeSentencesGeneration.isPresent()) {
				if (indicativeSentencesGeneration.get().separator().equals("")) {
					return IndicativeSentencesGeneration.DEFAULT_SEPARATOR;
				}
				return indicativeSentencesGeneration.get().separator();
			}

			return IndicativeSentencesGeneration.DEFAULT_SEPARATOR;
		}

		/**
		 * Gets the generator for {@link IndicativeSentencesGeneration} when extracting the
		 * annotation from {@code IndicativeSentencesGeneration}, if it doesn't find it,
		 * then search for the parent classes, if no generator value is found use
		 * {@link Standard} by default.
		 *
		 * @param testClass Class to get Indicative sentence generator either custom or default
		 * @return the {@code DisplayNameGenerator} instance to use in indicative sentences generator
		 */
		private DisplayNameGenerator getGeneratorForIndicativeSentence(Class<?> testClass) {
			Optional<IndicativeSentencesGeneration> indicativeSentencesGeneration = getIndicativeSentencesGeneration(
				testClass);
			if (indicativeSentencesGeneration.isPresent()) {
				DisplayNameGenerator displayNameGenerator = getDisplayNameGenerator(
					indicativeSentencesGeneration.get().generator());
				if (displayNameGenerator.getClass() == IndicativeSentences.class) {
					return getDisplayNameGenerator(IndicativeSentencesGeneration.DEFAULT_GENERATOR);
				}
				return displayNameGenerator;
			}

			return getDisplayNameGenerator(IndicativeSentencesGeneration.DEFAULT_GENERATOR);
		}

		/**
		 * Finds the {@code IndicativeSentencesGeneration} annotation that is present,
		 * meta-present or if it doesn't find it, then search for the enclosing
		 * parent classes, if no annotation is found returns empty.
		 *
		 * @param testClass the test class to find the {@code IndicativeSentencesGeneration}
		 * annotation
		 * @return the optional annotation retrieved from the test class.
		 */
		private Optional<IndicativeSentencesGeneration> getIndicativeSentencesGeneration(Class<?> testClass) {
			Optional<IndicativeSentencesGeneration> indicativeSentencesGeneration = findAnnotation(testClass,
				IndicativeSentencesGeneration.class);

			if (indicativeSentencesGeneration.isPresent()) {
				return indicativeSentencesGeneration;
			}
			if (testClass.getEnclosingClass() != null) {
				return getIndicativeSentencesGeneration(testClass.getEnclosingClass());
			}

			return Optional.empty();
		}
	}

	/**
	 * Return the {@code DisplayNameGenerator} instance corresponding to the
	 * given {@code Class}.
	 *
	 * @param generatorClass the generator's {@code Class}; never {@code null},
	 * has to be a {@code DisplayNameGenerator} implementation
	 * @return a {@code DisplayNameGenerator} implementation instance
	 */
	static DisplayNameGenerator getDisplayNameGenerator(Class<?> generatorClass) {
		Preconditions.notNull(generatorClass, "Class must not be null");
		Preconditions.condition(DisplayNameGenerator.class.isAssignableFrom(generatorClass),
			"Class must be a DisplayNameGenerator implementation");
		if (generatorClass == Standard.class) {
			return Standard.INSTANCE;
		}
		if (generatorClass == Simple.class) {
			return Simple.INSTANCE;
		}
		if (generatorClass == ReplaceUnderscores.class) {
			return ReplaceUnderscores.INSTANCE;
		}
		if (generatorClass == IndicativeSentences.class) {
			return IndicativeSentences.INSTANCE;
		}
		return (DisplayNameGenerator) ReflectionUtils.newInstance(generatorClass);
	}

}
