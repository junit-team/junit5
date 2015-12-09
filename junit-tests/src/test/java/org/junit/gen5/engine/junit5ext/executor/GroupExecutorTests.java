
package org.junit.gen5.engine.junit5ext.executor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.testdoubles.TestDescriptorStub;
import org.junit.gen5.engine.junit5ext.testdoubles.TestExecutorRegistrySpy;

public class GroupExecutorTests {
	private TestExecutorRegistrySpy testExecutorRegistrySpy = new TestExecutorRegistrySpy();
	private GroupDescriptor emptyGroupDescriptor = new GroupDescriptor("id", "name");
	private GroupDescriptor singleGroupDescriptor = new GroupDescriptor("id", "name");
	private GroupExecutor groupExecutor = new GroupExecutor();

	@Before
	public void setUp() throws Exception {
		groupExecutor.setTestExecutorRegistry(testExecutorRegistrySpy);
		singleGroupDescriptor.addChild(emptyGroupDescriptor);
	}

	@Test
	public void givenAnArbitraryDescriptor_executorDeclinesRequest() throws Exception {
		boolean result = groupExecutor.canExecute(new TestDescriptorStub());
		assertThat(result).isFalse();
	}

	@Test
	public void givenAGroupDescriptor_executorAcceptsRequest() throws Exception {
		boolean result = groupExecutor.canExecute(emptyGroupDescriptor);
		assertThat(result).isTrue();
	}

	@Test
	public void givenGroupWithoutChildren_noFurtherExecutionIsTriggered() throws Exception {
		groupExecutor.execute(null, emptyGroupDescriptor);
		assertThat(testExecutorRegistrySpy.testDescriptors).isEmpty();
	}

	@Test
	public void givenGroupWithOneChild_childExecutionIsTriggered() throws Exception {
		groupExecutor.execute(null, singleGroupDescriptor);
		assertThat(testExecutorRegistrySpy.testDescriptors).contains(emptyGroupDescriptor);
	}
}
