plugins {
	id("junitbuild.jacoco-conventions")
	`jacoco-report-aggregation`
}

val jacocoRootReport by reporting.reports.creating(JacocoCoverageReport::class) {
	testType = TestSuiteType.UNIT_TEST
}

val classesView = configurations["aggregateCodeCoverageReportResults"].incoming.artifactView {
	withVariantReselection() // Required to ensure the transformed classes are selected
	componentFilter { it is ProjectComponentIdentifier }
	attributes {
		attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class, LibraryElements.CLASSES))
	}
}

tasks.named<JacocoReport>(jacocoRootReport.reportTask.name).configure {
	// Override to restore behavior of pre-8.2.1 Gradle (see https://github.com/gradle/gradle/issues/25618)
	classDirectories.setFrom(classesView.files)
}
