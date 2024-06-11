@import java.util.List
@import junitbuild.generator.JRE

@param List<JRE> jres

package org.junit.jupiter.api.condition;

public class JavaVersionPredicates {

	private static final String JAVA_VERSION = System.getProperty("java.version");
@for(JRE jre : jres)
	static boolean onJava${jre.getVersion()}() {
		return JAVA_VERSION.startsWith("@if(jre.getVersion() == 8)1.@endif${jre.getVersion()}");
	}
@endfor
}
