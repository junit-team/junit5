@import java.util.List
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.JRE.*;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link DisabledOnJre}.
 *
 * @since 5.1
 */
class DisabledOnJreIntegrationTests {

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledOnJre({})
	void missingJreDeclaration() {
	}

	@Test
	@DisabledOnJre(value = { @for(var jre : jres)JAVA_${jre.getVersion()}, @endforOTHER }, disabledReason = "Disabled on every JRE")
	void disabledOnAllJavaVersions() {
		fail("should be disabled");
	}
@for(var jre : jres)
	@Test
	@DisabledOnJre(JAVA_${jre.getVersion()})
	void java${jre.getVersion()}() {
		assertFalse(onJava${jre.getVersion()}());
	}
@endfor
	@Test
	@DisabledOnJre(OTHER)
	void other() {
		assertTrue(onKnownVersion());
	}

}
