/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.condition.JRE.JAVA_10;
import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;
import static org.junit.jupiter.api.condition.JRE.JAVA_9;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;

class ConditionalTestExecutionDemo {

	// tag::user_guide_os[]
	@Test
	@EnabledOnOs(MAC)
	void onlyOnMacOs() {
		// ...
	}

	@TestOnMac
	void testOnMac() {
		// ...
	}

	@Test
	@EnabledOnOs({ LINUX, MAC })
	void onLinuxOrMac() {
		// ...
	}

	@Test
	@DisabledOnOs(WINDOWS)
	void notOnWindows() {
		// ...
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Test
	@EnabledOnOs(MAC)
	@interface TestOnMac {
	}
	// end::user_guide_os[]

	// tag::user_guide_architecture[]
	@Test
	@EnabledOnOs(architectures = "aarch64")
	void onAarch64() {
		// ...
	}

	@Test
	@DisabledOnOs(architectures = "x86_64")
	void notOnX86_64() {
		// ...
	}

	@Test
	@EnabledOnOs(value = MAC, architectures = "aarch64")
	void onNewMacs() {
		// ...
	}

	@Test
	@DisabledOnOs(value = MAC, architectures = "aarch64")
	void notOnNewMacs() {
		// ...
	}
	// end::user_guide_architecture[]

	// tag::user_guide_jre[]
	@Test
	@EnabledOnJre(JAVA_8)
	void onlyOnJava8() {
		// ...
	}

	@Test
	@EnabledOnJre({ JAVA_9, JAVA_10 })
	void onJava9Or10() {
		// ...
	}

	@Test
	@EnabledForJreRange(min = JAVA_9, max = JAVA_11)
	void fromJava9to11() {
		// ...
	}

	@Test
	@EnabledForJreRange(min = JAVA_9)
	void fromJava9toCurrentJavaFeatureNumber() {
		// ...
	}

	@Test
	@EnabledForJreRange(max = JAVA_11)
	void fromJava8To11() {
		// ...
	}

	@Test
	@DisabledOnJre(JAVA_9)
	void notOnJava9() {
		// ...
	}

	@Test
	@DisabledForJreRange(min = JAVA_9, max = JAVA_11)
	void notFromJava9to11() {
		// ...
	}

	@Test
	@DisabledForJreRange(min = JAVA_9)
	void notFromJava9toCurrentJavaFeatureNumber() {
		// ...
	}

	@Test
	@DisabledForJreRange(max = JAVA_11)
	void notFromJava8to11() {
		// ...
	}
	// end::user_guide_jre[]

	// tag::user_guide_native[]
	@Test
	@EnabledInNativeImage
	void onlyWithinNativeImage() {
		// ...
	}

	@Test
	@DisabledInNativeImage
	void neverWithinNativeImage() {
		// ...
	}
	// end::user_guide_native[]

	// tag::user_guide_system_property[]
	@Test
	@EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
	void onlyOn64BitArchitectures() {
		// ...
	}

	@Test
	@DisabledIfSystemProperty(named = "ci-server", matches = "true")
	void notOnCiServer() {
		// ...
	}
	// end::user_guide_system_property[]

	// tag::user_guide_environment_variable[]
	@Test
	@EnabledIfEnvironmentVariable(named = "ENV", matches = "staging-server")
	void onlyOnStagingServer() {
		// ...
	}

	@Test
	@DisabledIfEnvironmentVariable(named = "ENV", matches = ".*development.*")
	void notOnDeveloperWorkstation() {
		// ...
	}
	// end::user_guide_environment_variable[]

	// tag::user_guide_custom[]
	@Test
	@EnabledIf("customCondition")
	void enabled() {
		// ...
	}

	@Test
	@DisabledIf("customCondition")
	void disabled() {
		// ...
	}

	boolean customCondition() {
		return true;
	}
	// end::user_guide_custom[]

}
