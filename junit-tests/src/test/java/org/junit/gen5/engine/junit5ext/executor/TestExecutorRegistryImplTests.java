
package org.junit.gen5.engine.junit5ext.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.testdoubles.AlwaysMatchingTestExecutorStub;
import org.junit.gen5.engine.junit5ext.testdoubles.AlwaysNonMatchingTestExecutorStub;

public class TestExecutorRegistryImplTests {
	private GroupDescriptor testGroup = new GroupDescriptor("testGroup", "Test Group");
	private TestPlanSpecification emptyTestPlanSpecification = TestPlanSpecification.build();
	private TestExecutorRegistryImpl testExecutorRegistry = new TestExecutorRegistryImpl();

	@Test
	public void givenNoTestExecutors_lookupWillReturnAnEmptyList() throws Exception {
		List<TestExecutor> testExecutors = testExecutorRegistry.lookupExecutors(testGroup);
		assertThat(testExecutors).isEmpty();
	}

	@Test
	public void givenAMatchingTestExecutor_lookupReturnsExecutor() throws Exception {
		TestExecutor testExecutor = new AlwaysMatchingTestExecutorStub();
		testExecutorRegistry.register(testExecutor);

		List<TestExecutor> testExecutors = testExecutorRegistry.lookupExecutors(testGroup);
		assertThat(testExecutors).contains(testExecutor);
	}

	@Test
	public void givenNonMatchingTestExecutor_lookupReturnsAnEmptyList() throws Exception {
		TestExecutor testExecutor = new AlwaysNonMatchingTestExecutorStub();
		testExecutorRegistry.register(testExecutor);

		List<TestExecutor> testExecutors = testExecutorRegistry.lookupExecutors(testGroup);
		assertThat(testExecutors).isEmpty();
	}
}
