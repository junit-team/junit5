@import java.util.List
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava10;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava11;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava12;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava13;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava14;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava15;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava16;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava17;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava18;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava19;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava20;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava21;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava22;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava23;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava8;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava9;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link EnabledOnJreCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link EnabledOnJreIntegrationTests}.
 *
 * @since 5.1
 */
class EnabledOnJreConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new EnabledOnJreCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return EnabledOnJreIntegrationTests.class;
	}

	/**
	 * @see EnabledOnJreIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@EnabledOnJre is not present");
	}

	/**
	 * @see EnabledOnJreIntegrationTests#missingJreDeclaration()
	 */
	@Test
	void missingJreDeclaration() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("You must declare at least one JRE");
	}

	/**
	 * @see EnabledOnJreIntegrationTests#enabledOnAllJavaVersions()
	 */
	@Test
	void enabledOnAllJavaVersions() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(true);
	}
@for(var jre : jres)
	/**
	 * @see EnabledOnJreIntegrationTests#java${jre.getVersion()}()
	 */
	@Test
	void java${jre.getVersion()}() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(onJava${jre.getVersion()}());
	}
@endfor
	/**
	 * @see EnabledOnJreIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertEnabledOnCurrentJreIf(!onKnownVersion());
		assertCustomDisabledReasonIs("Disabled on almost every JRE");
	}

	private void assertEnabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + System.getProperty("java.version"));
		}
		else {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + System.getProperty("java.version"));
		}
	}

}
