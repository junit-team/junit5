@import java.util.List
@import gg.jte.support.ForSupport
@import junitbuild.generator.model.JRE

@param List<JRE> jres
@param String licenseHeader
${licenseHeader}
package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;
import java.util.EnumSet;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;
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
--%>JAVA_${jre.getVersion()}(${jre.getVersion()}),
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
	OTHER(Integer.MAX_VALUE);

	private static final Logger logger = LoggerFactory.getLogger(JRE.class);

	private static final int UNKNOWN_FEATURE_VERSION = -1;

	private static final int CURRENT_FEATURE_VERSION = determineCurrentFeatureVersion();

	private static final JRE CURRENT_VERSION = determineCurrentVersion(CURRENT_FEATURE_VERSION);

	private static int determineCurrentFeatureVersion() {
		String javaVersion = System.getProperty("java.version");
		boolean javaVersionIsBlank = StringUtils.isBlank(javaVersion);

		if (javaVersionIsBlank) {
			logger.debug(
				() -> "JVM system property 'java.version' is undefined. It is therefore not possible to detect Java 8.");
		}

		if (!javaVersionIsBlank && javaVersion.startsWith("1.8")) {
			return 8;
		}

		try {
			// java.lang.Runtime.version() is a static method available on Java 9+
			// that returns an instance of java.lang.Runtime.Version which has the
			// following method: public int major()
			Method versionMethod = Runtime.class.getMethod("version");
			Object version = ReflectionSupport.invokeMethod(versionMethod, null);
			Method majorMethod = version.getClass().getMethod("major");
			return (int) ReflectionSupport.invokeMethod(majorMethod, version);
		}
		catch (Exception ex) {
			logger.debug(ex, () -> "Failed to determine the current JRE version via java.lang.Runtime.Version.");
		}

		return UNKNOWN_FEATURE_VERSION;
	}

	private static JRE determineCurrentVersion(int currentFeatureVersion) {
		switch (currentFeatureVersion) {
			case UNKNOWN_FEATURE_VERSION:
				// null signals that the current JRE version is "unknown"
				return null;<%--
			--%>@for(var jre : jres)
			case ${jre.getVersion()}:
				return JAVA_${jre.getVersion()};<%--
			--%>@endfor
			default:
				return OTHER;
		}
	}

	private final int featureVersion;

	private JRE(int featureVersion) {
		this.featureVersion = featureVersion;
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
	 * Get the feature version of <em>this</em> {@code JRE}.
	 *
	 * @return the feature version of this {@code JRE}, or
	 * {@link Integer#MAX_VALUE} if this {@code JRE} is {@link #OTHER}
	 *
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	public int featureVersion() {
		return this.featureVersion;
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

	/**
	 * @return the feature version for the currently executing JVM, or
	 * {@code -1} to signal that the feature version is unknown
	 *
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	public static int currentFeatureVersion() {
		return CURRENT_FEATURE_VERSION;
	}

	/**
	 * @return {@code true} if the supplied feature version is known to be
	 * the Java Runtime Environment version for the currently executing JVM
	 * or if the supplied feature version is {@code -1} and the feature
	 * version of the current JVM is unknown
	 *
	 * @since 5.12
	 */
	@API(status = EXPERIMENTAL, since = "5.12")
	public static boolean isCurrentFeatureVersion(int featureVersion) {
		return featureVersion == CURRENT_FEATURE_VERSION;
	}

	static boolean isCurrentVersionWithinRange(JRE min, JRE max) {
		return EnumSet.range(min, max).contains(CURRENT_VERSION);
	}

	static boolean isCurrentVersionWithinRange(int min, int max) {
		return CURRENT_FEATURE_VERSION >= min && CURRENT_FEATURE_VERSION <= max;
	}

}
