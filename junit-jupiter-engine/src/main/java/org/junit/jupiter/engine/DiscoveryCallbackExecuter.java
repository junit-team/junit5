
package org.junit.jupiter.engine;

import org.junit.platform.engine.TestDescriptor;

public class DiscoveryCallbackExecuter {

	void executeAllCallbacks(TestDescriptor engineDescriptor) {

		TestDescriptor.Visitor visitor = (descriptor) -> executeCallbacks(descriptor);
		engineDescriptor.accept(visitor);
	}

	void executeCallbacks(TestDescriptor testDescriptor) {
		// TODO - for now a no-op
		System.out.println("Visiting: " + testDescriptor.getUniqueId());
	}

}
