/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;

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
@API(status = EXPERIMENTAL, since = "5.4")
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
	 * {@code DisplayNameGenerator} that replaces underscores with spaces.
	 *
	 * <p>This generator extends the functionality of {@link Standard} by
	 * replacing all underscores ({@code '_'}) found in class and method names
	 * with spaces ({@code ' '}).
	 */
	class ReplaceUnderscores extends Standard {

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
			String displayName = replaceUnderscores(testMethod.getName());
			if (hasParameters(testMethod)) {
				displayName += ' ' + parameterTypesAsString(testMethod);
			}
			return displayName;
		}

		private static String replaceUnderscores(String name) {
			return name.replace('_', ' ');
		}

		private static boolean hasParameters(Method method) {
			return method.getParameterCount() > 0;
		}
	}

	/**
	 * {@code DisplayNameGenerator} that supports {@code ReplaceUnderscores} to generate complete sentences.
	 *
	 * <p>This generator extends the functionality of {@link ReplaceUnderscores} by
	 * replacing all underscores ({@code '_'}) found in class and method names
	 * with spaces ({@code ' '}).
	 */
	class IndicativeSentencesGenerator extends ReplaceUnderscores {

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			String indicativeName = removeRootCharacter(super.generateDisplayNameForClass(testClass));
			return indicativeName;
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			String indicativeName = classReplaceToIndicativeSentence(nestedClass);
			return indicativeName;
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			String methodName = underscoreSnakeCaseName(super.generateDisplayNameForMethod(testClass,testMethod));
			String indicativeName = classReplaceToIndicativeSentence(testClass);

			return indicativeName + ", " + methodName ;
		}

		private String classReplaceToIndicativeSentence(Class<?> testClass) {
			String classIndicativeSentence = null;
			Class<?> classWithEnclosingParent = testClass.getEnclosingClass();
			DisplayName classWithDisplayName = testClass.getAnnotation(DisplayName.class);
			DisplayNameGeneration classWithAnnotation = testClass.getAnnotation(DisplayNameGeneration.class);

				if(classWithEnclosingParent == null ) {
					if(classWithDisplayName != null)
						classIndicativeSentence = classWithDisplayName.value();
					else
						classIndicativeSentence = generateDisplayNameForClass(testClass);
				}
				else {
					if(classWithAnnotation != null) {
						if (classWithAnnotation.value() == IndicativeSentencesGenerator.class) {
							if(classWithDisplayName != null)
								classIndicativeSentence = classWithDisplayName.value();
							else
								classIndicativeSentence = generateDisplayNameForClass(testClass);
						}
					}
					else {
						if(classWithDisplayName != null)
							classIndicativeSentence = classWithDisplayName.value();
						else
							classIndicativeSentence = super.generateDisplayNameForNestedClass(testClass);

						classIndicativeSentence = classReplaceToIndicativeSentence(classWithEnclosingParent) +
										", " +  classIndicativeSentence;
					}
				}

			return underscoreSnakeCaseName(classIndicativeSentence);
		}

		private String removeRootCharacter(String testName)
		{
			int indexOfChar = testName.indexOf('$');

			if(testName.indexOf('$')>=0)
				testName = testName.substring(indexOfChar+1);

			return testName;
		}

		private String underscoreSnakeCaseName(String testName)
		{
			if(testName.length() <= 1)
				return testName;
			else
			{
				String [] splitTestWords = testName.split(" ");

				for (int i = 1; i < splitTestWords.length; i++) {
					if (checkTwoCapsString(splitTestWords[i]) == false)
						splitTestWords[i] = Character.toLowerCase(splitTestWords[i].charAt(0)) + splitTestWords[i].substring(1);
				}

				return String.join(" ",splitTestWords);
			}
		}

		private boolean checkTwoCapsString(String inputWord) {
			char fragmentChar;
			int lowerCaseCount = 0;

			for(int i=0;i < inputWord.length();i++) {
				fragmentChar = inputWord.charAt(i);
				if (Character.isUpperCase(fragmentChar))
					lowerCaseCount++;
			}

			return (lowerCaseCount >=2);
		}
	}

}
