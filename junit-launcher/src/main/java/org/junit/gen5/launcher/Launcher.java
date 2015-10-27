package org.junit.gen5.launcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.engine.Engine;
import org.junit.gen5.engine.EngineTestDescription;

public class Launcher {

	private final Map<String, Map<TestIdentifier, EngineTestDescription>> testDescriptionsByEngine = new LinkedHashMap<>();

	public TestPlan discoverTests(String className) {
		TestPlan testPlan = new TestPlan();
		for (Engine engine : discoverEngines()) {
			testPlan.addTestIdentifiers(discoverTests(className, engine));
		}
		return testPlan;
	}

	private Set<TestIdentifier> discoverTests(String className, Engine engine) {
		List<EngineTestDescription> discoveredTests = engine.discoverTests(className);
		
		Map<TestIdentifier, EngineTestDescription> engineTestDescriptionsByTestId = new LinkedHashMap<>();
		discoveredTests.forEach(testDescription -> engineTestDescriptionsByTestId.put(
				new TestIdentifier(engine.getId(), testDescription.getId(), testDescription.getDisplayName()),
				testDescription));
		testDescriptionsByEngine.put(engine.getId(), engineTestDescriptionsByTestId);

		return engineTestDescriptionsByTestId.keySet();
	}

	// TODO no exceptions please
	public void execute(TestPlan testPlan) throws Exception {
		for (Engine engine : discoverEngines()) {
			Map<TestIdentifier, EngineTestDescription> engineTestDescriptions = testDescriptionsByEngine
					.get(engine.getId());
			List<TestIdentifier> testIdentifiers = testPlan.getTestIdentifiers();
			List<TestIdentifier> filtered = engineTestDescriptions.keySet().stream()
					.filter(testIdentifier -> testIdentifiers.contains(testIdentifier)).collect(Collectors.toList());
			List<EngineTestDescription> testDescriptions = new ArrayList<>();
			for (TestIdentifier testIdentifier : filtered) {
				testDescriptions.add(lookup(engine, testIdentifier));
			}
			engine.execute(testDescriptions);
		}
	}

	private EngineTestDescription lookup(Engine engine, TestIdentifier testIdentifier) {
		return testDescriptionsByEngine.get(engine.getId()).get(testIdentifier);
	}

	private Iterable<Engine> discoverEngines() {
		return ServiceLoader.load(Engine.class);
	}

}
