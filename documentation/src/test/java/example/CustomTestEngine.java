
package example;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;

/**
 * This is a no-op {@link TestEngine} that is only
 * used to make examples compile.
 */
class CustomTestEngine implements TestEngine {

	@Override
	public String getId() {
		return null;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		return null;
	}

	@Override
	public void execute(ExecutionRequest request) {
	}

}
