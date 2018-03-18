package org.junit.jupiter.theories.annotations;

import org.junit.jupiter.theories.TheoriesTestExtension;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.theories.annotations.suppliers.ArgumentsSuppliedBy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A theory is a specialized test that specifies one more "theory parameters". The theory will be repeatedly executed with different parameters in such a way
 * that the every combination of applicable data points will be passed to the theory parameters. Theories may be safely used in the same class as normal and
 * parameterized tests.
 * <p>
 * Datapoints are the values that will be passed to theory parameters, and are specified via the {@link DataPoint} and {@link DataPoints} annotations. See those
 * classes for additional details regarding what elements may be annotated and any additional requirements/limitations. Booleans and enums are considered
 * well-known types, and will be automatically populated if no data points of those types are provided ({@code true} and {@code false} for boolean parameters,
 * and every value in an enum for enum parameters).
 * <p>
 * Basic example:
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
 *     public void testUserIsRegistered(@TheoryParam String username, @TheoryParam boolean expectedResult) {
 *         if (expectedResult) {
 *             RegisteredUsers.addUser(username);
 *         }
 *         boolean actualResult = RegisteredUsers.userIsRegistered(username);
 *         assertEquals(expectedResult, actualResult);
 *     }
 * }
 * </pre>
 * <p>
 * This test will be called six times (3 string data points * 2 boolean data points). Since boolean is a well-know type, no data points have to be specified.
 * <p>
 * The set of applicable data points for a theory parameter can be further limited via qualifiers. (See {@link Qualifiers#value()} for details about how
 * qualifiers work.)
 * <p>
 * You can also specify the exact values for a theory parameter using parameter argument supplier annotations. For example, the
 * {@link org.junit.jupiter.theories.annotations.suppliers.IntValues} annotation may be used to specify an array of integer values to test against.
 * Parameter argument supplier annotations are specified via the {@link ArgumentsSuppliedBy}
 * meta-annotation. See annotations in the package {@link org.junit.jupiter.theories.annotations.suppliers} for provided suppliers.
 */
//TODO: Revamp documentation now that TheoryParams has been removed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TestTemplate
@ExtendWith(TheoriesTestExtension.class)
public @interface Theory {
    /**
     * Placeholder for the {@linkplain TestInfo#getDisplayName display name} of
     * a {@code @TheoryTest}
     */
    String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

    /**
     * Placeholder for the current permutation count of a {@code @Theory}
     */
    String CURRENT_PERMUTATION_PLACEHOLDER = "{currentPermutation}";

    /**
     * Placeholder for the total number of permutations of a {@code @Theory}
     */
    String TOTAL_PERMUTATIONS_PLACEHOLDER = "{totalPermutations}";

    /**
     * Placeholder the list of parameters values that will be passed into a {@code @{@link Theory}}.
     */
    String PARAMETER_VALUES_PLACEHOLDER = "{parameterValues}";

    /**
     * Placeholder the list of parameters values (with their indices) that will be passed into a {@code @{@link Theory}}.
     */
    String PARAMETER_VALUES_WITH_INDEXES_PLACEHOLDER = "{parameterValuesWithIndices}";

    /**
     * Placeholder the list of parameter details (value, source, index, etc.) that will be passed into a {@code @{@link Theory}}.
     */
    String PARAMETER_DETAILS_PLACEHOLDER = "{parameterDetails}";

    String displayName() default CURRENT_PERMUTATION_PLACEHOLDER + " of " + TOTAL_PERMUTATIONS_PLACEHOLDER;
}
