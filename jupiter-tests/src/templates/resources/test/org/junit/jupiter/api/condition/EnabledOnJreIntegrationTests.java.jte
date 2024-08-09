@import java.util.List
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param List<JRE> jresSortedByStringValue
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
@for(var jre : jresSortedByStringValue)<%--
--%>import static org.junit.jupiter.api.condition.JRE.JAVA_${jre.getVersion()};
@endfor<%--
--%>import static org.junit.jupiter.api.condition.JRE.OTHER;
@for(var jre : jresSortedByStringValue)<%--
--%>import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava${jre.getVersion()};
@endfor<%--
--%>import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link EnabledOnJre}.
 *
 * @since 5.1
 */
class EnabledOnJreIntegrationTests {

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledOnJre({})
	void missingJreDeclaration() {
	}

	@Test
	@EnabledOnJre({ //
@for(var jre : jres)<%--
--%>			JAVA_${jre.getVersion()}, //
@endfor<%--
--%>			OTHER //
	})
	void enabledOnAllJavaVersions() {
	}
@for(var jre : jres)
	@Test
	@EnabledOnJre(JAVA_${jre.getVersion()})
	void java${jre.getVersion()}() {
		assertTrue(onJava${jre.getVersion()}());
	}
@endfor
	@Test
	@EnabledOnJre(value = OTHER, disabledReason = "Disabled on almost every JRE")
	void other() {
		assertFalse(onKnownVersion());
	}

}
