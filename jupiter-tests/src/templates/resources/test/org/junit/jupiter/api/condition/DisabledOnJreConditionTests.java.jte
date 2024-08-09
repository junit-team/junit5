@import java.util.List
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param List<JRE> jresSortedByStringValue
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
@for(var jre : jresSortedByStringValue)<%--
--%>import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava${jre.getVersion()};
@endfor<%--
--%>import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link DisabledOnJreCondition}.
 *
 * <p>Note that test method names MUST match the test method names in
 * {@link DisabledOnJreIntegrationTests}.
 *
 * @since 5.1
 */
class DisabledOnJreConditionTests extends AbstractExecutionConditionTests {

	@Override
	protected ExecutionCondition getExecutionCondition() {
		return new DisabledOnJreCondition();
	}

	@Override
	protected Class<?> getTestClass() {
		return DisabledOnJreIntegrationTests.class;
	}

	/**
	 * @see DisabledOnJreIntegrationTests#enabledBecauseAnnotationIsNotPresent()
	 */
	@Test
	void enabledBecauseAnnotationIsNotPresent() {
		evaluateCondition();
		assertEnabled();
		assertReasonContains("@DisabledOnJre is not present");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#missingJreDeclaration()
	 */
	@Test
	void missingJreDeclaration() {
		Exception exception = assertThrows(PreconditionViolationException.class, this::evaluateCondition);
		assertThat(exception).hasMessageContaining("You must declare at least one JRE");
	}

	/**
	 * @see DisabledOnJreIntegrationTests#disabledOnAllJavaVersions()
	 */
	@Test
	void disabledOnAllJavaVersions() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(true);
		assertCustomDisabledReasonIs("Disabled on every JRE");
	}
@for(var jre : jres)
	/**
	 * @see DisabledOnJreIntegrationTests#java${jre.getVersion()}()
	 */
	@Test
	void java${jre.getVersion()}() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(onJava${jre.getVersion()}());
	}
@endfor
	/**
	 * @see DisabledOnJreIntegrationTests#other()
	 */
	@Test
	void other() {
		evaluateCondition();
		assertDisabledOnCurrentJreIf(!onKnownVersion());
	}

	private void assertDisabledOnCurrentJreIf(boolean condition) {
		if (condition) {
			assertDisabled();
			assertReasonContains("Disabled on JRE version: " + System.getProperty("java.version"));
		}
		else {
			assertEnabled();
			assertReasonContains("Enabled on JRE version: " + System.getProperty("java.version"));
		}
	}

}
