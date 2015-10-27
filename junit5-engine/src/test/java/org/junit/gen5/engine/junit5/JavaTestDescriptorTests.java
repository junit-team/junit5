
package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.gen5.api.Test;

/**
 * Unit tests for {@link JavaTestDescriptor}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class JavaTestDescriptorTests {

	private static final String JUNIT_5_ENGINE_ID = "junit5";

	private static final String TEST_METHOD_ID = JavaTestDescriptorTests.class.getName() + "#test()";
	private static final String TEST_METHOD_UID = JUNIT_5_ENGINE_ID + ":" + TEST_METHOD_ID;

	private static final String TEST_METHOD_STRING_BIGDECIMAL_ID = JavaTestDescriptorTests.class.getName()
			+ "#test(java.lang.String, java.math.BigDecimal)";
	private static final String TEST_METHOD_STRING_BIGDECIMAL_UID = JUNIT_5_ENGINE_ID + ":" + TEST_METHOD_STRING_BIGDECIMAL_ID;


	@org.junit.Test
	public void constructFromMethod() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("test");
		JavaTestDescriptor descriptor = new JavaTestDescriptor(JUNIT_5_ENGINE_ID, testMethod);

		System.out.println("DEBUG - " + descriptor);
		assertEqual(JUNIT_5_ENGINE_ID, descriptor.getEngineId());
		assertEqual(TEST_METHOD_ID, descriptor.getTestId());
		assertEqual(TEST_METHOD_UID, descriptor.getId());
		assertEqual(getClass(), descriptor.getTestClass());
		assertEqual(testMethod, descriptor.getTestMethod());
		assertEqual("test", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void constructFromMethodWithCustomDisplayName() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("foo");
		JavaTestDescriptor descriptor = new JavaTestDescriptor(JUNIT_5_ENGINE_ID, testMethod);

		System.out.println("DEBUG - " + descriptor);
		assertEqual(JUNIT_5_ENGINE_ID, descriptor.getEngineId());
		assertEqual(getClass(), descriptor.getTestClass());
		assertEqual(testMethod, descriptor.getTestMethod());
		assertEqual("custom test name", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void constructFromMethodWithParameters() throws Exception {
		Method testMethod = getClass().getDeclaredMethod("test", String.class, BigDecimal.class);
		JavaTestDescriptor descriptor = new JavaTestDescriptor(JUNIT_5_ENGINE_ID, testMethod);

		System.out.println("DEBUG - " + descriptor);
		assertEqual(JUNIT_5_ENGINE_ID, descriptor.getEngineId());
		assertEqual(TEST_METHOD_STRING_BIGDECIMAL_ID, descriptor.getTestId());
		assertEqual(TEST_METHOD_STRING_BIGDECIMAL_UID, descriptor.getId());
		assertEqual(getClass(), descriptor.getTestClass());
		assertEqual(testMethod, descriptor.getTestMethod());
		assertEqual("test", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void fromTestDescriptorIdForMethod() throws Exception {
		JavaTestDescriptor descriptor = JavaTestDescriptor.from(TEST_METHOD_UID);
		assertNotNull(descriptor, "descriptor:");
		assertEqual("test", descriptor.getDisplayName(), "display name:");
		assertEqual(JavaTestDescriptorTests.class, descriptor.getTestClass());
		assertEqual(JavaTestDescriptorTests.class.getDeclaredMethod("test"), descriptor.getTestMethod());
	}

	@org.junit.Test
	public void fromTestDescriptorIdForMethodWithParameters() throws Exception {
		JavaTestDescriptor descriptor = JavaTestDescriptor.from(TEST_METHOD_STRING_BIGDECIMAL_UID);
		assertNotNull(descriptor, "descriptor:");
		assertEqual("test", descriptor.getDisplayName(), "display name:");
		assertEqual(getClass(), descriptor.getTestClass());
		assertEqual(getClass().getDeclaredMethod("test", String.class, BigDecimal.class), descriptor.getTestMethod());
	}


	void test() {
	}

	void test(String txt, BigDecimal sum) {
	}

	@Test(name = "custom test name")
	void foo() {
	}

}
