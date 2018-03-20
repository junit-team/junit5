
package org.junit.jupiter.theories.annotations.suppliers;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.*;

import org.apiguardian.api.API;
import org.junit.jupiter.theories.suppliers.IntTheoryArgumentSupplier;

/**
 * Parameter argument supplier annotation that can be added to a theory
 * parameter to specify the exact values that will be used for that parameter.
 * Provides {@code int} values.
 */
@Target(ElementType.PARAMETER)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSuppliedBy(IntTheoryArgumentSupplier.class)
@API(status = EXPERIMENTAL, since = "5.2")
public @interface IntValues {
	/**
	 * @return the value(s) to use for the annotated theory parameter
	 */
	int[] value();
}
