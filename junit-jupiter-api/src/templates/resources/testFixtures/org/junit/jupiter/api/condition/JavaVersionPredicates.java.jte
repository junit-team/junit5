@import java.util.List
@import gg.jte.support.ForSupport
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

public class JavaVersionPredicates {

	private static final String JAVA_VERSION = System.getProperty("java.version");
@for(JRE jre : jres)
	static boolean onJava${jre.getVersion()}() {
		return JAVA_VERSION.startsWith("@if(jre.getVersion() == 8)1.@endif${jre.getVersion()}");
	}
@endfor
	static boolean onKnownVersion() {
		return @for(var jre : ForSupport.of(jres))onJava${jre.get().getVersion()}()@if(!jre.isLast()) //
				|| @endif@endfor;
	}
}
