package org.junit.gen5.launcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.engine.Engine;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlan;

public class Launcher {

	private final Map<String, Map<TestIdentifier, TestDescriptor>> testDescriptionsByEngine = new LinkedHashMap<>();

	public TestExecutionPlan discoverTests(TestPlan testPlan) {
		TestExecutionPlan executionPlan = new TestExecutionPlan();
		for (Engine engine : discoverEngines()) {
			executionPlan.addTestIdentifiers(discoverTests(testPlan, engine));
		}
		return executionPlan;
	}

	private Set<TestIdentifier> discoverTests(TestPlan testPlan, Engine engine) {
		List<TestDescriptor> discoveredTests = engine.discoverTests(testPlan);
		
		Map<TestIdentifier, TestDescriptor> engineTestDescriptionsByTestId = new LinkedHashMap<>();
		discoveredTests.forEach(testDescription -> engineTestDescriptionsByTestId.put(
				new TestIdentifier(engine.getId(), testDescription.getId(), testDescription.getDisplayName()),
				testDescription));
		testDescriptionsByEngine.put(engine.getId(), engineTestDescriptionsByTestId);

		return engineTestDescriptionsByTestId.keySet();
	}

	// TODO no exceptions please
	public void execute(TestExecutionPlan testPlan) throws Throwable {
		for (Engine engine : discoverEngines()) {
			Map<TestIdentifier, TestDescriptor> engineTestDescriptions = testDescriptionsByEngine
					.get(engine.getId());
			List<TestIdentifier> testIdentifiers = testPlan.getTestIdentifiers();
			List<TestIdentifier> filtered = engineTestDescriptions.keySet().stream()
					.filter(testIdentifier -> testIdentifiers.contains(testIdentifier)).collect(Collectors.toList());
			List<TestDescriptor> testDescriptions = new ArrayList<>();
			for (TestIdentifier testIdentifier : filtered) {
				testDescriptions.add(lookup(engine, testIdentifier));
			}
			engine.execute(testDescriptions);
		}
	}

	private TestDescriptor lookup(Engine engine, TestIdentifier testIdentifier) {
		return testDescriptionsByEngine.get(engine.getId()).get(testIdentifier);
	}

	private Iterable<Engine> discoverEngines() {
		return ServiceLoader.load(Engine.class);
	}

}
