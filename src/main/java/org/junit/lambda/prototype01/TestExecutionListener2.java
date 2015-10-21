package org.junit.lambda.prototype01;

public interface TestExecutionListener2 {

	void before(ClassExecutionContext context);

	void before(MethodExecutionContext context);

	void after(MethodExecutionContext context);

	void after(ClassExecutionContext context);

}
