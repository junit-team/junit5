plugins {
	id("junitbuild.jacoco-conventions")
	`jacoco-report-aggregation`
}

reporting {
	reports {
		create<JacocoCoverageReport>("jacocoRootReport") {
			testType = TestSuiteType.UNIT_TEST
		}
	}
}
