
package org.junit.jupiter.engine.discovery.predicates;

import org.junit.jupiter.api.Test;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IsPotentialTestContainerTests {

    private final Predicate<Class<?>> isPotentialTestContainer = new IsPotentialTestContainer();

    @Test
    void abstractClassEvaluatesToFalse() {
        assertFalse(isPotentialTestContainer.test(AbstractClass.class));
    }

    @Test
    void localClassEvaluatesToFalse() {

        class LocalClass{
        }

        assertFalse(isPotentialTestContainer.test(LocalClass.class));
    }

    @Test
    void anonymousClassEvaluatesToFalse() {

        Object object = new Object() {
            @Override
            public String toString() {
                return "";
            }
        };

        assertFalse(isPotentialTestContainer.test(object.getClass()));
    }

    @Test
    void staticClassEvaluatesToTrue() {
        assertTrue(isPotentialTestContainer.test(StaticClass.class));
    }

    static class StaticClass {
    }

}


abstract class AbstractClass {
}
