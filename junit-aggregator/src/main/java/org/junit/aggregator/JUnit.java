package org.junit.aggregator;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.spi.ToolProvider;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

@API(status = EXPERIMENTAL, since = "6.0")
public final class JUnit {

	private JUnit() {
	}
	
	public static void run(Object instance) {
		if (instance instanceof Class<?> testClass) {
			run(testClass);
		} else {
			run(instance.getClass());
		}
	}

	public static void run(Class<?> testClass) {
		var listener = new SummaryGeneratingListener();
		var request = request() //
				.selectors(selectClass(testClass)) //
				.forExecution() //
				.listeners(listener) //
				.build();
		LauncherFactory.create().execute(request);
		var summary = listener.getSummary();
		if (!summary.getFailures().isEmpty()) {
			summary.printFailuresTo(new PrintWriter(System.err, true, Charset.defaultCharset()));
			throw new JUnitException("There are test failures!");
		}
	}

	public static void main(String[] args) {
		var junit = ToolProvider.findFirst("junit").orElseThrow();
		var exitCode = junit.run(System.out, System.err, args);
		System.exit(exitCode);
	}
}
