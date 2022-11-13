import org.gradle.api.attributes.TestSuiteType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.`jacoco-report-aggregation`
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport

plugins {
	id("jacoco-conventions")
	`jacoco-report-aggregation`
}

reporting {
	reports {
		create<JacocoCoverageReport>("jacocoRootReport") {
			testType.set(TestSuiteType.UNIT_TEST)
		}
	}
}
