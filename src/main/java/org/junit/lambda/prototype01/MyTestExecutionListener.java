package org.junit.lambda.prototype01;

public class MyTestExecutionListener implements TestExecutionListener {

	@Override
	public void before(TestExecutionContext context) {
		if (context instanceof ClassExecutionContext) {
			ClassExecutionContext classContext = (ClassExecutionContext) context;
			classContext.getTestClass();
		}
		else if (context instanceof MethodExecutionContext) {
			MethodExecutionContext methodContext = (MethodExecutionContext) context;
			methodContext.getTestMethod();
		}
	}

	@Override
	public void after(TestExecutionContext context) {
		// TODO Auto-generated method stub

	}

}
