
package org.junit.engine.core;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.core.Assertions.*;

/**
 * Unit tests for {@link JavaTestDescriptor}.
 *
 * <p>To execute tests, simply run the {@link #main} method. The lack of
 * an exception means the tests passed.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class JavaTestDescriptorTestCase {

	private static final String JUNIT_5_ENGINE_ID = "junit5";

	private static final String TEST_METHOD_ID = "org.junit.engine.core.JavaTestDescriptorTestCase#test()";
	private static final String TEST_METHOD_UID = JUNIT_5_ENGINE_ID + ":" + TEST_METHOD_ID;

	private static final String TEST_METHOD_STRING_BIGDECIMAL_ID = "org.junit.engine.core.JavaTestDescriptorTestCase#test(java.lang.String, java.math.BigDecimal)";
	private static final String TEST_METHOD_STRING_BIGDECIMAL_UID = JUNIT_5_ENGINE_ID + ":" + TEST_METHOD_STRING_BIGDECIMAL_ID;


	public static void main(String... args) throws Exception {
		constructor();
		from();
	}

	private static void constructor() throws Exception {
		Class<?> testClass = JavaTestDescriptorTestCase.class;
		Method testMethod = testClass.getDeclaredMethod("test");
		JavaTestDescriptor descriptor = new JavaTestDescriptor(JUNIT_5_ENGINE_ID, testClass, testMethod);

		System.out.println("DEBUG - " + descriptor);
		assertEqual(JUNIT_5_ENGINE_ID, descriptor.getEngineId());
		assertEqual(TEST_METHOD_ID, descriptor.getTestId());

		testMethod = testClass.getDeclaredMethod("test", String.class, BigDecimal.class);
		descriptor = new JavaTestDescriptor(JUNIT_5_ENGINE_ID, testClass, testMethod);

		System.out.println("DEBUG - " + descriptor);
		assertEqual(JUNIT_5_ENGINE_ID, descriptor.getEngineId());
		assertEqual(TEST_METHOD_STRING_BIGDECIMAL_ID, descriptor.getTestId());
	}

	private static void from() throws Exception {
		JavaTestDescriptor descriptor = JavaTestDescriptor.from(TEST_METHOD_UID);
		assertNotNull("descriptor:", descriptor);
		assertEqual("display name:", "test", descriptor.getDisplayName());
		assertEqual(JavaTestDescriptorTestCase.class, descriptor.getTestClass());
		assertEqual(JavaTestDescriptorTestCase.class.getDeclaredMethod("test"), descriptor.getTestMethod());

		descriptor = JavaTestDescriptor.from(TEST_METHOD_STRING_BIGDECIMAL_UID);
		assertNotNull("descriptor:", descriptor);
		assertEqual("display name:", "test", descriptor.getDisplayName());
		assertEqual(JavaTestDescriptorTestCase.class, descriptor.getTestClass());
		assertEqual(JavaTestDescriptorTestCase.class.getDeclaredMethod("test", String.class, BigDecimal.class),
			descriptor.getTestMethod());
	}

	void test() {
	}

	void test(String txt, BigDecimal sum) {
	}

}
