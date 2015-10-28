
package org.junit.gen5.engine.junit5;

import static java.util.stream.Collectors.*;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;

/**
 * @author Sam Brannen
 * @author Matthias Merdes
 * @since 5.0
 */
class TestExecutor {

	private final JavaTestDescriptor testDescriptor;


	TestExecutor(JavaTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	void execute() throws Exception {
		Class<?> testClass = this.testDescriptor.getTestClass();

		// TODO Extract test instantiation
		Object testInstance = newInstance(testClass);

		executeBeforeMethods(testClass, testInstance);
		invokeMethod(this.testDescriptor.getTestMethod(), testInstance);
		executeAfterMethods(testClass, testInstance);
	}

	private void executeBeforeMethods(Class<?> testClass, Object testInstance) throws Exception {
		for (Method method : findAnnotatedMethods(testClass, Before.class)) {
			invokeMethod(method, testInstance);
		}
	}

	private void executeAfterMethods(Class<?> testClass, Object testInstance) throws Exception {
		for (Method method : findAnnotatedMethods(testClass, After.class)) {
			invokeMethod(method, testInstance);
		}
	}

	private List<Method> findAnnotatedMethods(Class<?> testClass, Class<? extends Annotation> annotationType) {
		return Arrays.stream(testClass.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(annotationType))
				.collect(toList());
	}

}
