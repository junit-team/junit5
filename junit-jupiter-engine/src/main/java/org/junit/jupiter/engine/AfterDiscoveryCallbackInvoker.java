
package org.junit.jupiter.engine;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.AfterDiscoveryCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestDescriptor;

public class AfterDiscoveryCallbackInvoker {

	static final Class<? extends Extension> AFTER_DISCOVERY_CALLBACK_TYPE = AfterDiscoveryCallback.class;
	static final List<Class<? extends Extension>> EXTENSION_TYPES = Arrays.asList(AFTER_DISCOVERY_CALLBACK_TYPE);

	void invokeAfterDiscoveryCallbacks(TestDescriptor engineDescriptor) {
		TestDescriptor.Visitor visitor = (testDescriptor) -> {
			ExtensionRegistry registry = getExtensionRegistry(testDescriptor);
			registry.stream(AfterDiscoveryCallback.class).forEach(
				(extension) -> executeAndMaskThrowable(() -> extension.afterDiscovery((testDescriptor))));
		};
		engineDescriptor.accept(visitor);
	}

	protected void executeAndMaskThrowable(Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable throwable) {
			ExceptionUtils.throwAsUncheckedException(throwable);
		}
	}

	ExtensionRegistry getExtensionRegistry(TestDescriptor testDescriptor) {
		ExtensionRegistry defaultRegistry = ExtensionRegistry.createRegistryWithDefaultExtensions();
		return defaultRegistry;
	}

}
