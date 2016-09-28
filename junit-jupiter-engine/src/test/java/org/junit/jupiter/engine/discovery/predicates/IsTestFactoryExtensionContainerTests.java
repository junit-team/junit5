package org.junit.jupiter.engine.discovery.predicates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestFactoryExtension;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IsTestFactoryExtensionContainerTests {

	private final Predicate<Class<?>> isTestFactoryExtensionContainer = new IsTestFactoryExtensionContainer();

	@Test
	void standardTestClassEvaluatesToFalse() {
		assertFalse(isTestFactoryExtensionContainer.test(ClassWithoutTestFactoryExtension.class));
	}

	@Test
	void classWithTestFactoryExtensionOnMethodEvaluatesToFalse() {
		assertFalse(isTestFactoryExtensionContainer.test(ClassWithTestFactoryExtensionOnMethod.class));
	}

	@Test
	void classWithTestFactoryExtensionOnClassEvaluatesToTrue() {
		assertTrue(isTestFactoryExtensionContainer.test(ClassWithTestFactoryExtension.class));
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithoutTestFactoryExtension {

	@Test
	void test() {
	}

}

class ClassWithTestFactoryExtensionOnMethod {

	@ExtendWith(TestFactoryExtension.class)
	void test() {
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
@ExtendWith(TestFactoryExtension.class)
class ClassWithTestFactoryExtension {

}

