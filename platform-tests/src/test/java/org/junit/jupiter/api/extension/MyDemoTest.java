package org.junit.jupiter.api.extension;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@ExtendWith(MyInvocationInterceptor.class)
class MyDemoTest {
    static {
        System.out.println("MyTest, static initialization");
    }

    // uncommenting enforces static initialization
    // BEFORE InvocationInterceptor.interceptTestClassConstructor() is invoked.
        @BeforeAll
        static void beforeAll() {
            System.out.println("MyTest, beforeAll");
        }

    @Test
    void dummy() {
        System.out.println("MyTest, dummy test");
    }
}
