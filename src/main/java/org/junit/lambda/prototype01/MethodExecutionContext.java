package org.junit.lambda.prototype01;

import java.lang.reflect.Method;

public interface MethodExecutionContext extends TestExecutionContext {

	Method getTestMethod();
	
}
