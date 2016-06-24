
package org.junit.jupiter.engine.discovery.predicates;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 5.0
 */
public class IsNestedTestClassTests {

	private final Predicate<Class<?>> isNestedTestClass = new IsNestedTestClass();


    @Test
    void nestedClassEvaluatesToTrue() {
        assertTrue(isNestedTestClass.test(ClassWithNestedInnerClasses.InnerClass.class));
    }

    @Test
    void staticNestedClassEvaluatesToFalse() {
        assertFalse(isNestedTestClass.test(ClassWithNestedInnerClasses.StaticInnerClass.class));
    }

    @Test
    void privateNestedClassEvaluatesToFalse() {
        // @formatter:off
        Class<?> privateInnerClass = Arrays.stream(ClassWithInnerClasses.class.getDeclaredClasses())
                .filter(aClass -> aClass.getSimpleName().equals("PrivateInnerClass"))
                .findFirst()
                .get();
		// @formatter:on

        assertFalse(isNestedTestClass.test(privateInnerClass));
    }

}

class ClassWithNestedInnerClasses {

	@Nested
	class InnerClass {
	}

    @SuppressWarnings("unused")
	@Nested
	private class PrivateInnerClass {
	}

	@Nested
	static class StaticInnerClass {
	}

}
