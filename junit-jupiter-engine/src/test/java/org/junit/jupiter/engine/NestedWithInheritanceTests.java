package org.junit.jupiter.engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class NestedWithInheritanceTests extends SuperClass {

    static List<String> lifecycleInvokingClassNames;

    static String OUTER = NestedWithInheritanceTests.class.getSimpleName();
    static String NESTED = NestedClass.class.getSimpleName();
    static String NESTEDNESTED = NestedClass.NestedNestedClass.class.getSimpleName();



    @Nested
    class NestedClass extends SuperClass {

        @Test
        public void test() {
            assertThat(lifecycleInvokingClassNames).containsExactly(OUTER, NESTED);
        }

        @Nested
        class NestedNestedClass extends SuperClass {

            @Test
            public void test() {
                assertThat(lifecycleInvokingClassNames).containsExactly(OUTER, NESTED, NESTEDNESTED );
            }
        }

    }

}

class SuperClass {

    @BeforeAll
    static void setup() {
        NestedWithInheritanceTests.lifecycleInvokingClassNames = new ArrayList<>();
    }

    @BeforeEach
    public void beforeEach() {
        String invokingClass = this.getClass().getSimpleName();
        System.out.println("beforeEach() --> " + invokingClass);
        NestedWithInheritanceTests.lifecycleInvokingClassNames.add(invokingClass);
    }

}

