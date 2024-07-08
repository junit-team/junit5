@import java.util.List
@import gg.jte.support.ForSupport
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;
import java.util.EnumSet;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

/**
 * Enumeration of Java Runtime Environment (JRE) versions.
 *
 * <p>If the current JRE version cannot be detected &mdash; for example, if the
 * {@code java.version} JVM system property is undefined &mdash; then none of
 * the constants defined in this enum will be considered to be the
 * {@linkplain #isCurrentVersion current JRE version}.
 *
 * @since 5.1
@for(JRE jre : jres)<%--
--%> * @see #JAVA_${jre.getVersion()}
@endfor<%--
--%> * @see #OTHER
 * @see EnabledOnJre
 * @see DisabledOnJre
 * @see EnabledForJreRange
 * @see DisabledForJreRange
 */
@API(status = STABLE, since = "5.1")
public enum JRE {
@for(var jre : jres)
	/**
	 * Java ${jre.getVersion()}.
	@if(jre.getSince() != null)<%--
--%> *
	 * @since ${jre.getSince()}
	@endif<%--
--%> */
	@if(jre.getSince() != null)<%--
--%>@API(status = STABLE, since = "${jre.getSince()}")
	@endif<%--
--%>JAVA_${jre.getVersion()},
@endfor
	/**
	 * A JRE version other than <%--
--%>@for(var jre : ForSupport.of(jres))<%--
	--%>@if(jre.isLast())or @endif<%--
	--%>{@link #JAVA_${jre.get().getVersion()}}<%--
	--%>@if(jre.isLast()).@else,@endif<%--
	--%>@if(jre.getIndex() % 3 == 1 && !jre.isLast())
	 * @elseif(!jre.isLast()) @endif<%--
--%>@endfor
	 */
	OTHER;

	private static final Logger logger = LoggerFactory.getLogger(JRE.class);

	private static final JRE CURRENT_VERSION = determineCurrentVersion();

	private static JRE determineCurrentVersion() {
		String javaVersion = System.getProperty("java.version");
		boolean javaVersionIsBlank = StringUtils.isBlank(javaVersion);

		if (javaVersionIsBlank) {
			logger.debug(
				() -> "JVM system property 'java.version' is undefined. It is therefore not possible to detect Java 8.");
		}

		if (!javaVersionIsBlank && javaVersion.startsWith("1.8")) {
			return JAVA_8;
		}

		try {
			// java.lang.Runtime.version() is a static method available on Java 9+
			// that returns an instance of java.lang.Runtime.Version which has the
			// following method: public int major()
			Method versionMethod = Runtime.class.getMethod("version");
			Object version = ReflectionUtils.invokeMethod(versionMethod, null);
			Method majorMethod = version.getClass().getMethod("major");
			int major = (int) ReflectionUtils.invokeMethod(majorMethod, version);
			switch (major) {<%--
				--%>@for(var jre : jres)<%--
					--%>@if(jre.getVersion() != 8)
				case ${jre.getVersion()}:
					return JAVA_${jre.getVersion()};<%--
					--%>@endif<%--
				--%>@endfor
				default:
					return OTHER;
			}
		}
		catch (Exception ex) {
			logger.debug(ex, () -> "Failed to determine the current JRE version via java.lang.Runtime.Version.");
		}

		// null signals that the current JRE version is "unknown"
		return null;
	}

	/**
	 * @return {@code true} if <em>this</em> {@code JRE} is known to be the
	 * Java Runtime Environment version for the currently executing JVM or if
	 * the version is {@link #OTHER}
	 */
	public boolean isCurrentVersion() {
		return this == CURRENT_VERSION;
	}

	/**
	 * @return the {@link JRE} for the currently executing JVM, potentially
	 * {@link #OTHER}
	 *
	 * @since 5.7
	 */
	@API(status = STABLE, since = "5.7")
	public static JRE currentVersion() {
		return CURRENT_VERSION;
	}

	static boolean isCurrentVersionWithinRange(JRE min, JRE max) {
		return EnumSet.range(min, max).contains(CURRENT_VERSION);
	}

}
