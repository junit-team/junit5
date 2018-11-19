import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.platform.commons.util.ModuleUtils;

class JUnitPlatformCommonsTests {

	@Test
	@EnabledOnJre(JRE.JAVA_8)
	void onJava8() {
		assertFalse(ModuleUtils.isJavaPlatformModuleSystemAvailable());
		assertFalse(ModuleUtils.getModuleName(Object.class).isPresent());
	}

	@Test
	@DisabledOnJre(JRE.JAVA_8)
	void onJava9OrHigher() {
		assertTrue(ModuleUtils.isJavaPlatformModuleSystemAvailable());
		assertEquals("java.base", ModuleUtils.getModuleName(Object.class).orElseThrow(Error::new));
	}

}
