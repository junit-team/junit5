
package org.junit.gen5.engine;

public abstract class TreeBasedTestEngine implements TestEngine {

	@Override
	public abstract TestDescriptor discoverTests(TestPlanSpecification specification);

	@Override
	public final void execute(ExecutionRequest request) {
		try {
			TestDescriptor rootTestDescriptor = request.getRootTestDescriptor();
			executeAll(rootTestDescriptor, request.getEngineExecutionListener(), new Context());
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private <T> void executeAll(TestDescriptor parentDescriptor, EngineExecutionListener listener,
			Context parentContext) throws Exception {
		Context context = parentContext;
		if (parentDescriptor instanceof Parent) {
			context = ((Parent) parentDescriptor).beforeAll(context);
		}
		for (TestDescriptor childDescriptor : parentDescriptor.getChildren()) {
			if (childDescriptor instanceof Child) {
				Child child = (Child) childDescriptor;
				try {
					listener.testStarted(childDescriptor);
					context = child.execute(context);
					listener.testSucceeded(childDescriptor);
				}
				catch (Throwable t) {
					listener.testFailed(childDescriptor, t);
					context = context.with("Throwable", t);
				}
			}
			executeAll(childDescriptor, listener, context);
		}
		if (parentDescriptor instanceof Parent) {
			context = ((Parent) parentDescriptor).afterAll(context);
		}
	}

}
