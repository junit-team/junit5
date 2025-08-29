/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.ANONYMOUS_CLASSES;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.TOP_LEVEL_CLASSES;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;
import static com.tngtech.archunit.core.domain.properties.HasModifiers.Predicates.modifier;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameContaining;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameStartingWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.onlyBeAccessedByClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.have;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.PackageMatcher;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

import org.apiguardian.api.API;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.MediaType;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.support.ParameterInfo;
import org.junit.platform.commons.support.Resource;
import org.junit.platform.commons.support.scanning.ClassFilter;
import org.junit.platform.commons.support.scanning.ClasspathScanner;
import org.junit.platform.commons.support.scanning.DefaultClasspathScanner;
import org.junit.platform.commons.util.ModuleUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

@AnalyzeClasses(packages = { "org.junit.platform", "org.junit.jupiter", "org.junit.vintage" })
class ArchUnitTests {

	@SuppressWarnings("unused")
	@ArchTest
	private final ArchRule allClassesAreInJUnitPackage = classes() //
			.should().haveNameMatching("org\\.junit\\..+");

	@SuppressWarnings("unused")
	@ArchTest
	private final ArchRule allPublicTopLevelTypesHaveApiAnnotations = classes() //
			.that(have(modifier(PUBLIC))) //
			.and(TOP_LEVEL_CLASSES) //
			.and(not(ANONYMOUS_CLASSES)) //
			.and(not(describe("are Kotlin SAM type implementations", simpleName("")))) //
			.and(not(describe("are Kotlin-generated classes that contain only top-level functions",
				simpleNameEndingWith("Kt")))) //
			.and(not(describe("are shadowed", resideInAPackage("..shadow..")))) //
			.should().beAnnotatedWith(API.class);

	@SuppressWarnings("unused")
	@ArchTest // Consistency of @Documented and @Inherited is checked by the compiler but not for @Retention and @Target
	private final ArchRule repeatableAnnotationsShouldHaveMatchingContainerAnnotations = classes() //
			.that(nameStartingWith("org.junit.")) //
			.and().areAnnotations() //
			.and().areAnnotatedWith(Repeatable.class) //
			.should(haveContainerAnnotationWithSameRetentionPolicy()) //
			.andShould(haveContainerAnnotationWithSameTargetTypes());

	private final DescribedPredicate<? super JavaClass> jupiterAssertions = name(Assertions.class.getName()) //
			.or(name(Assumptions.class.getName())).or(name("org.junit.jupiter.api.AssertionsKt"));

	@SuppressWarnings("unused")
	@ArchTest // https://github.com/junit-team/junit-framework/issues/4604
	private final ArchRule jupiterAssertionsShouldBeSelfContained = classes().that(jupiterAssertions) //
			.should(onlyBeAccessedByClassesThat(jupiterAssertions));

	@ArchTest
	void packagesShouldBeNullMarked(JavaClasses classes) {
		var exclusions = Stream.of( //
			"..shadow.." //
		).map(PackageMatcher::of).toList();

		var subpackages = Stream.of("org.junit.platform", "org.junit.jupiter", "org.junit.vintage") //
				.map(classes::getPackage) //
				.flatMap(rootPackage -> rootPackage.getSubpackagesInTree().stream()) //
				.filter(pkg -> exclusions.stream().noneMatch(it -> it.matches(pkg.getName()))) //
				.filter(pkg -> !pkg.getClasses().isEmpty()) //
				.toList();
		assertThat(subpackages).isNotEmpty();

		var violations = subpackages.stream() //
				.filter(pkg -> !pkg.isAnnotatedWith(NullMarked.class)) //
				.map(JavaPackage::getName) //
				.sorted();
		assertThat(violations).describedAs("The following packages are missing the @NullMarked annotation").isEmpty();
	}

	@ArchTest
	void allAreIn(JavaClasses classes) {
		// about 928 classes found in all jars
		assertTrue(classes.size() > 800, "expected more than 800 classes, got: " + classes.size());
	}

	@ArchTest
	void freeOfGroupCycles(JavaClasses classes) {
		slices().matching("org.junit.(*)..").should().beFreeOfCycles().check(classes);
	}

	@ArchTest
	void freeOfPackageCycles(JavaClasses classes) throws Exception {
		slices().matching("org.junit.(**)").should().beFreeOfCycles() //

				// https://github.com/junit-team/junit-framework/issues/4886
				.ignoreDependency(TestReporter.class, MediaType.class) //

				// https://github.com/junit-team/junit-framework/issues/4885
				.ignoreDependency(ModuleUtils.class, Resource.class) //
				.ignoreDependency(
					Class.forName("org.junit.platform.commons.util.ModuleUtils$ModuleReferenceResourceScanner"),
					Resource.class) //
				.ignoreDependency(ReflectionUtils.class, Resource.class) //
				.ignoreDependency(ClasspathScanner.class, Resource.class) //
				.ignoreDependency(DefaultClasspathScanner.class, Resource.class) //

				// Avoid using Preconditions
				.ignoreDependency(ClassFilter.class, Preconditions.class) //

				// Move DefaultClasspathScanner to org.junit.platform.commons.util?
				.ignoreDependency(DefaultClasspathScanner.class, Preconditions.class) //
				.ignoreDependency(DefaultClasspathScanner.class, StringUtils.class) //
				.ignoreDependency(DefaultClasspathScanner.class, UnrecoverableExceptions.class) //

				// Needs more investigation
				.ignoreDependency(resideInAPackage("org.junit.platform.console.options"),
					resideInAPackage("org.junit.platform.console.tasks"))

				// Needs more investigation
				.ignoreDependency(ParameterInfo.class, ArgumentsAccessor.class)

				// Needs more investigation
				.ignoreDependency(OutputDirectoryProvider.class, TestDescriptor.class) //

				// Needs more investigation
				.ignoreDependency(NamespacedHierarchicalStore.class, ThrowableCollector.class) //

				.check(classes);
	}

	@ArchTest
	void avoidJavaUtilLogging(JavaClasses classes) {
		// LoggerFactory.java:80 -> sets field LoggerFactory$DelegatingLogger.julLogger
		var subset = classes.that(are(not(name("org.junit.platform.commons.logging.LoggerFactory$DelegatingLogger"))));
		GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(subset);
	}

	@ArchTest
	void avoidThrowingGenericExceptions(JavaClasses classes) {
		// LoggerFactory.java:155 -> new Throwable()
		var subset = classes.that(are(not(
			name("org.junit.platform.commons.logging.LoggerFactory$DelegatingLogger").or(nameContaining(".shadow.")))));
		GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(subset);
	}

	@ArchTest
	void avoidAccessingStandardStreams(JavaClasses classes) {
		// ConsoleLauncher, StreamInterceptor, Picocli et al...
		var subset = classes //
				.that(are(not(name("org.junit.platform.console.ConsoleLauncher")))) //
				.that(are(not(name("org.junit.platform.console.tasks.ConsoleTestExecutor")))) //
				.that(are(not(name("org.junit.platform.launcher.core.StreamInterceptor")))) //
				.that(are(not(name("org.junit.platform.testkit.engine.Events")))) //
				.that(are(not(name("org.junit.platform.testkit.engine.Executions")))) //
				//The PreInterruptThreadDumpPrinter writes to StdOut by contract to dump threads
				.that(are(not(name("org.junit.jupiter.engine.extension.PreInterruptThreadDumpPrinter")))) //
				.that(are(not(nameContaining(".shadow."))));
		GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(subset);
	}

	private static ArchCondition<? super JavaClass> haveContainerAnnotationWithSameRetentionPolicy() {
		return ArchCondition.from(new RepeatableAnnotationPredicate<>(Retention.class,
			(expectedTarget, actualTarget) -> expectedTarget.value() == actualTarget.value()));
	}

	private static ArchCondition<? super JavaClass> haveContainerAnnotationWithSameTargetTypes() {
		return ArchCondition.from(new RepeatableAnnotationPredicate<>(Target.class,
			(expectedTarget, actualTarget) -> Arrays.equals(expectedTarget.value(), actualTarget.value())));
	}

	private static class RepeatableAnnotationPredicate<T extends Annotation> extends DescribedPredicate<JavaClass> {

		private final Class<T> annotationType;
		private final BiPredicate<T, T> predicate;

		RepeatableAnnotationPredicate(Class<T> annotationType, BiPredicate<T, T> predicate) {
			super("have identical @%s annotation as container annotation", annotationType.getSimpleName());
			this.annotationType = annotationType;
			this.predicate = predicate;
		}

		@Override
		public boolean test(JavaClass annotationClass) {
			var containerAnnotationClass = (JavaClass) annotationClass.getAnnotationOfType(
				Repeatable.class.getName()).get("value").orElseThrow();
			var expectedAnnotation = annotationClass.tryGetAnnotationOfType(annotationType);
			var actualAnnotation = containerAnnotationClass.tryGetAnnotationOfType(annotationType);
			return expectedAnnotation.map(expectedTarget -> actualAnnotation //
					.map(actualTarget -> predicate.test(expectedTarget, actualTarget)) //
					.orElse(false)) //
					.orElse(actualAnnotation.isEmpty());
		}
	}

}
