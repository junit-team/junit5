package org.junit.lambda.prototype01;

public interface TestExecutionListener {

	void before(TestExecutionContext context);

	void after(TestExecutionContext context);

}
