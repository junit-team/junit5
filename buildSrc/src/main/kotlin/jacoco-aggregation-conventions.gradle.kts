import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.TestSuiteType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.`jacoco-report-aggregation`
import org.gradle.kotlin.dsl.named
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport
import org.junit.gradle.jacoco.JacocoConventions.COVERAGE_CLASSES

plugins {
	id("jacoco-conventions")
	`jacoco-report-aggregation`
}

configurations {
	allCodeCoverageReportClassDirectories {
		attributes {
			attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class, COVERAGE_CLASSES))
		}
	}
}

reporting {
	reports {
		create<JacocoCoverageReport>("jacocoRootReport") {
			testType.set(TestSuiteType.UNIT_TEST)
		}
	}
}
