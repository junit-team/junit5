package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

/**
 * Integration tests that verify support for {@link TestInstancePreDestroyCallback}.
 *
 * @since 5.6
 */
public class TestInstancePreDestroyCallbackTests extends AbstractJupiterTestEngineTests {

    private static final List<String> callSequence = new ArrayList<>();

    @BeforeEach
    void resetCallSequence() {
        callSequence.clear();
    }

    @Test
    void instancePostProcessorsInNestedClasses() {
        executeTestsForClass(OuterTestCase.class).testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));

        // @formatter:off
        assertThat(callSequence).containsExactly(

			// OuterTestCase
            "fooPostProcessTestInstance:OuterTestCase",
            	"beforeOuterMethod",
            		"testOuter",
             "fooPreDestroyCallbackTestInstance:OuterTestCase",

			// InnerTestCase
			"fooPostProcessTestInstance:OuterTestCase",
			"fooPostProcessTestInstance:InnerTestCase",
				"barPostProcessTestInstance:InnerTestCase",
					"beforeOuterMethod",
						"beforeInnerMethod",
							"testInner",
                "barPreDestroyCallbackTestInstance:InnerTestCase",
            "fooPreDestroyCallbackTestInstance:InnerTestCase"
            // TODO: missing second fooPreDestroyCallbackTestInstance:OuterTestCase?
        );
        // @formatter:on
    }

    @Test
    void testSpecificTestInstancePreDestroyCallbackIsCalled() {
        executeTestsForClass(TestCaseWithTestSpecificTestInstancePreDestroyCallback.class).testEvents().assertStatistics(//
                stats -> stats.started(1).succeeded(1));

        // @formatter:off
        assertThat(callSequence).containsExactly(
                "fooPostProcessTestInstance:TestCaseWithTestSpecificTestInstancePreDestroyCallback",
                	"beforeEachMethod",
                		"test",
                			"fooPreDestroyCallbackTestInstance:TestCaseWithTestSpecificTestInstancePreDestroyCallback"
        );
        // @formatter:on
    }

    // -------------------------------------------------------------------

    @ExtendWith(FooInstancePostProcessor.class)
    @ExtendWith(FooInstancePreDestroyCallback.class)
    static class OuterTestCase extends Named {

        @BeforeEach
        void beforeOuterMethod() {
            callSequence.add("beforeOuterMethod");
        }

        @Test
        void testOuter() {
            callSequence.add("testOuter");
        }

        @Nested
        @ExtendWith(BarInstancePostProcessor.class)
        @ExtendWith(BarInstancePreDestroyCallback.class)
        class InnerTestCase extends Named {

            @BeforeEach
            void beforeInnerMethod() {
                callSequence.add("beforeInnerMethod");
            }

            @Test
            void testInner() {
                callSequence.add("testInner");
            }
        }
    }

    static class TestCaseWithTestSpecificTestInstancePreDestroyCallback extends Named {

        @BeforeEach
        void beforeEachMethod() {
            callSequence.add("beforeEachMethod");
        }

        @ExtendWith(FooInstancePostProcessor.class)
        @ExtendWith(FooInstancePreDestroyCallback.class)
        @Test
        void test() {
            callSequence.add("test");
        }
    }

    static class FooInstancePostProcessor implements TestInstancePostProcessor {

        @Override
        public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
            if (testInstance instanceof Named) {
                ((Named) testInstance).setName("foo:" + context.getRequiredTestClass().getSimpleName());
            }
            callSequence.add("fooPostProcessTestInstance:" + testInstance.getClass().getSimpleName());
        }
    }

    static class BarInstancePostProcessor implements TestInstancePostProcessor {

        @Override
        public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
            if (testInstance instanceof Named) {
                ((Named) testInstance).setName("bar:" + context.getRequiredTestClass().getSimpleName());
            }
            callSequence.add("barPostProcessTestInstance:" + testInstance.getClass().getSimpleName());
        }
    }

    static class FooInstancePreDestroyCallback implements TestInstancePreDestroyCallback {

        @Override
        public void preDestroyTestInstance(Object testInstance, ExtensionContext context) throws Exception {
            if (testInstance instanceof Named) {
                String name;
                if("InnerTestCase".equals(testInstance.getClass().getSimpleName())) {
                    name = "bar";
                } else {
                    name = "foo";
                }
                assertSame(testInstance, context.getTestInstance().get());
                assertEquals(name + ":" + testInstance.getClass().getSimpleName(), ((Named) testInstance).getName());
            }
            callSequence.add("fooPreDestroyCallbackTestInstance:" + testInstance.getClass().getSimpleName());
        }
    }

    static class BarInstancePreDestroyCallback implements TestInstancePreDestroyCallback {

        @Override
        public void preDestroyTestInstance(Object testInstance, ExtensionContext context) throws Exception {
            if (testInstance instanceof Named) {
                assertSame(testInstance, context.getTestInstance().get());
                assertEquals("bar:" + testInstance.getClass().getSimpleName(), ((Named) testInstance).getName());
            }
            callSequence.add("barPreDestroyCallbackTestInstance:" + testInstance.getClass().getSimpleName());
        }
    }

    private abstract static class Named {

        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
