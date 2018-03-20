
package org.junit.jupiter.theories.annotations.suppliers;

import java.lang.annotation.*;

import org.junit.jupiter.theories.suppliers.ByteTheoryArgumentSupplier;

/**
 * Parameter argument supplier annotation that can be added to a theory
 * parameter to specify the exact values that will be used for that parameter.
 * Provides {@code byte} values.
 */
@Target(ElementType.PARAMETER)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSuppliedBy(ByteTheoryArgumentSupplier.class)
public @interface ByteValues {
	/**
	 * @return the value(s) to use for the annotated theory parameter
	 */
	byte[] value();
}
