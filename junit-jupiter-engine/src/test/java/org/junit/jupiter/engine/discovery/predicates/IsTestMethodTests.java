
package org.junit.jupiter.engine.discovery.predicates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IsTestMethodTests {

	private final Predicate<Method> isTestMethod = new IsTestMethod();

    @Test
	void publicTestMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method publicTestMethod = this.findMethod("publicTestMethod");
		assertTrue(isTestMethod.test(publicTestMethod));
	}

	@Test
	void publicTestMethodsWithArgumentEvaluatesToTrue() throws NoSuchMethodException {
		Method publicTestMethodWithArgument = findMethod("publicTestMethodWithArgument", TestInfo.class);
		assertTrue(isTestMethod.test(publicTestMethodWithArgument));
	}

	@Test
	void protectedTestMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method protectedTestMethod = this.findMethod("protectedTestMethod");
		assertTrue(isTestMethod.test(protectedTestMethod));
	}

	@Test
	void packageVisibleTestMethodTestMethodsEvaluatesToTrue() throws NoSuchMethodException {
		Method packageVisibleTestMethod = this.findMethod("packageVisibleTestMethod");
		assertTrue(isTestMethod.test(packageVisibleTestMethod));
	}

	@Test
	void privateTestMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method privateTestMethod = this.findMethod("privateTestMethod");
		assertFalse(isTestMethod.test(privateTestMethod));
	}

	@Test
	void staticTestMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method staticTestMethod = this.findMethod("staticTestMethod");
		assertFalse(isTestMethod.test(staticTestMethod));
	}

	@Test
	void abstractTestMethodEvaluatesToFalse() throws NoSuchMethodException {
		Method abstractTestMethod = this.findMethodOfAbstractClass("abstractTestMethod");
		assertFalse(isTestMethod.test(abstractTestMethod));
	}

	private Method findMethod(String name, Class<?>... aClass) {
		return ReflectionUtils.findMethod(ClassWithTestMethods.class, name, aClass).get();
	}

	private Method findMethodOfAbstractClass(String name) {
		return ReflectionUtils.findMethod(AbstractClassWithTestMethod.class, name).get();
	}

}

//name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithTestMethods {

	@Test
	public void publicTestMethod() {
	}

	@Test
	public void publicTestMethodWithArgument(TestInfo info) {
	}

	@Test
	protected void protectedTestMethod() {
	}

	@Test
	void packageVisibleTestMethod() {
	}

	@Test
	private void privateTestMethod() {
	}

	@Test
	static void staticTestMethod() {
	}

}

//name must not end with 'Tests', otherwise it would be picked up by the suite
abstract class AbstractClassWithTestMethod {

	@Test
	abstract void abstractTestMethod();

}
