package org.junit.platform.engine.support.hierarchical;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.annotation.UseResource;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

class ParallelExecutionIntegrationTests {

	@Test
	void pullUpExclusiveChildResourcesToTestClass() {
		NodeExecutor<?> engineNodeExecutor = createRootNodeExecutor(TestCaseWithResourceLock.class);

		assertThat(engineNodeExecutor.getChildren()).hasSize(1);
		NodeExecutor<?> testClassExecutor = engineNodeExecutor.getChildren().get(0);
		assertThat(testClassExecutor.getResourceLock()).isInstanceOf(CompositeLock.class);

		assertThat(testClassExecutor.getChildren()).hasSize(1);
		NodeExecutor<?> testMethodExecutor = testClassExecutor.getChildren().get(0);
		assertThat(testMethodExecutor.getResourceLock()).isInstanceOf(NopLock.class);
	}

	@Test
	void leavesResourceLockOnTestMethodWhenClassDoesNotUseResource() {
		NodeExecutor<?> engineNodeExecutor = createRootNodeExecutor(TestCaseWithoutResourceLock.class);

		assertThat(engineNodeExecutor.getChildren()).hasSize(1);
		NodeExecutor<?> testClassExecutor = engineNodeExecutor.getChildren().get(0);
		assertThat(testClassExecutor.getResourceLock()).isInstanceOf(NopLock.class);

		assertThat(testClassExecutor.getChildren()).hasSize(2);
		NodeExecutor<?> testMethodExecutor = testClassExecutor.getChildren().get(0);
		assertThat(testMethodExecutor.getResourceLock()).isInstanceOf(SingleLock.class);
		NodeExecutor<?> nestedTestClassExecutor = testClassExecutor.getChildren().get(1);
		assertThat(nestedTestClassExecutor.getResourceLock()).isInstanceOf(CompositeLock.class);

		assertThat(nestedTestClassExecutor.getChildren()).hasSize(1);
		NodeExecutor<?> nestedTestMethodExecutor = nestedTestClassExecutor.getChildren().get(0);
		assertThat(nestedTestMethodExecutor.getResourceLock()).isInstanceOf(NopLock.class);
	}

	private NodeExecutor<?> createRootNodeExecutor(Class<?> testClass) {
		LauncherDiscoveryRequest discoveryRequest = request()
				.selectors(selectClass(testClass))
				.build();
		TestDescriptor testDescriptor = new JupiterTestEngine().discover(discoveryRequest, UniqueId.forEngine("junit-jupiter"));
		ExecutionRequest executionRequest = new ExecutionRequest(testDescriptor, null, null);
		HierarchicalTestExecutor<?> executor = new HierarchicalTestExecutor<>(executionRequest, null, null);
		return executor.createRootNodeExecutor();
	}

	@UseResource("a")
	static class TestCaseWithResourceLock {
		@Test
		@UseResource("b")
		void test() {}
	}

	static class TestCaseWithoutResourceLock {
		@Test
		@UseResource("a")
		void test() {}

		@Nested
		@UseResource("c")
		class NestedTestCaseWithResourceLock {
			@Test
			@UseResource("b")
			void test() {}
		}
	}
}
