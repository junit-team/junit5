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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.theories.suppliers.ArgumentsSuppliedBy;

/**
 * A theory is a specialized test that will be repeatedly executed with
 * different parameters in such a way that the every combination of
 * applicable data points will be passed to the parameters that can accept
 * them. Theories may be safely used in the same class as normal and
 * parameterized tests.
 *
 * <p>Data points are the values that will be passed to theory parameters, and
 * are specified via the {@link DataPoint} and {@link DataPoints} annotations.
 * See those classes for additional details regarding what elements may be
 * annotated and any additional requirements/limitations. Booleans and enums
 * are considered well-known types, and will be automatically populated if no
 * data points of those types are provided ({@code true} and {@code false} for
 * boolean parameters, and every value in an enum for enum parameters).
 *
 * <p>Basic example:
 * <pre>
 * public class RegisteredUsersTest {
 *     &#064;DataPoint
 *     public static final String ALPHA_USERNAME = "someUser";
 *
 *     &#064;DataPoint
 *     public static final String ALPHANUMERIC_USERNAME = "someUser1234";
 *
 *     &#064;DataPoint
 *     public static final String ALPHA_WITH_DASHES_USERNAME = "some-user";
 *
 *     &#064;Theory
 *     public void testUserIsRegistered(String username, boolean expectedResult) {
 *         if (expectedResult) {
 *             RegisteredUsers.addUser(username);
 *         }
 *         boolean actualResult = RegisteredUsers.userIsRegistered(username);
 *         assertEquals(expectedResult, actualResult);
 *     }
 * }
 * </pre>
 *
 * <p>This test will be called six times (3 string data points * 2 boolean data
 * points). Since boolean is a well-know type, no data points have to be
 * specified.
 *
 * <p>The set of applicable data points for a theory parameter can be further
 * limited via qualifiers. (See {@link Qualifiers#value()} for details about
 * how qualifiers work.)
 *
 * <p>You can also specify the exact values for a theory parameter using
 * parameter argument supplier annotations. For example, the
 * {@link org.junit.jupiter.theories.suppliers.IntValues}
 * annotation may be used to specify an array of integer values to test
 * against. Parameter argument supplier annotations are specified via the
 * {@link ArgumentsSuppliedBy} meta-annotation. See annotations in the package
 * {@link org.junit.jupiter.theories.suppliers} for provided
 * suppliers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TestTemplate
@ExtendWith(TheoriesTestExtension.class)
@API(status = EXPERIMENTAL, since = "5.3")
public @interface Theory {
	/**
	 * Placeholder for the {@linkplain TestInfo#getDisplayName display name} of
	 * a {@code @TheoryTest}
	 */
	String DISPLAY_NAME_PLACEHOLDER = "{name}";

	/**
	 * Placeholder for the current permutation count of a {@code @Theory}
	 */
	String CURRENT_PERMUTATION_PLACEHOLDER = "{currentPermutation}";

	/**
	 * Placeholder for the total number of permutations of a {@code @Theory}
	 */
	String TOTAL_PERMUTATIONS_PLACEHOLDER = "{totalPermutations}";

	/**
	 * Placeholder for the list of argument values that will be passed into a
	 * {@code @{@link Theory}}.
	 */
	String ARGUMENT_VALUES_PLACEHOLDER = "{argumentValues}";

	/**
	 * Placeholder for the list of argument values (with their indices) that
	 * will be passed into a {@code @{@link Theory}}. This is more detailed
	 * than {@code ARGUMENT_VALUES_PLACEHOLDER} and less detailed than
	 * {@code ARGUMENT_DETAILS_PLACEHOLDER}.
	 */
	String ARGUMENT_VALUES_WITH_INDEXES_PLACEHOLDER = "{argumentValuesWithIndices}";

	/**
	 * Placeholder for the list of argument details (value, source, index, etc.)
	 * that will be passed into a {@code @{@link Theory}}.
	 */
	String ARGUMENT_DETAILS_PLACEHOLDER = "{argumentDetails}";

	String name() default CURRENT_PERMUTATION_PLACEHOLDER + " of " + TOTAL_PERMUTATIONS_PLACEHOLDER + " [Values: "
			+ ARGUMENT_VALUES_PLACEHOLDER + "]";
}
