package org.junit.jupiter.theories.annotations;

import org.apiguardian.api.API;

import java.lang.annotation.*;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Annotation that limits the data points that are applicable for a {@link Theory}.
 */
@Target(ElementType.PARAMETER)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@API(status = EXPERIMENTAL, since = "5.2")
public @interface Qualifiers {
    /**
     * The qualifier(s) for this data point parameter. If qualifiers are provided, the annotated parameter will be run only with data points that have the
     * at least one of the provided qualifiers. For example, if you have this data point:
     * <p>
     * {@code @DataPoint(qualifiers = {"multipleOfTen", "multipleOfTwo"})}<br>
     * {@code public static final int TEN = 10;}
     * <p>
     * Would be used by these theory parameters:
     * <p>
     * {@code public void testWithTens(@TheoryParam(qualifiers = "multipleOfTen") int testValue)}<br>
     * {@code public void testWithTwosAndThrees(@TheoryParam(qualifiers = {"multipleOfTwo", "multipleOfThree"}))}
     * <p>
     * However, this theory parameter would NOT use the data point:
     * <p>
     * {@code public void testWithFours(@TheoryParam(qualifiers = {"multipleOfFour}))}
     *
     * @return the qualifier(s), if any (can be empty)
     */
    String[] value();
}
