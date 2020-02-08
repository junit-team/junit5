/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
@file:JvmName("TestCaseBuilder")
@file:Suppress(JAVA_ONLY_HACK, "NOTHING_TO_INLINE")

package org.junit.jupiter.api

import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
import java.util.function.Function

// Implementation Notes
//
// - The name functions are annotated with @JvmSynthetic because Kotlin has
//   different visibility rules than Java and they would be visible in Java if
//   we would not mark them as such.
// - The various component functions are also annotated with @JvmSynthetic since
//   Java users cannot destructure the objects like Kotlin users can but instead
//   shall use the fields directly. This minimized overhead for them but also
//   minimized the public API we are exposing to Java, making it simpler to use.
// - The whole Self business in Params is required for covariance and ensures
//   that the concrete classes stay minimal.
// - The arity of 22 was not chosen arbitrarily, it's the number Kotlin uses for
//   its lambdas: https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/jvm/runtime/kotlin/jvm/functions/Functions.kt

/**
 * [TestCase] encapsulates the parameters for a parameterized test that can be
 * constructed through one of the [testOf] static factory functions. It enables
 * customization of the [name] of each individual case and (through its
 * subclasses) provides compile-time type safety for them. Instances are created
 * through one of the [case], or caseOf in Java, functions.
 *
 * @since 5.7
 * @see org.junit.jupiter.api.case
 * @see org.junit.jupiter.api.testOf
 * @see org.junit.jupiter.api.TestCaseIterator
 */
@API(status = EXPERIMENTAL, since = "5.7")
@Suppress("UNCHECKED_CAST") // covariance
sealed class TestCase<Self : TestCase<Self>> {
    private var name: (Self.() -> String)? = null

    @JvmSynthetic
    protected abstract fun name(): String

    /**
     * Generate a dynamic [name] for this case.
     *
     * @see TestCaseIterator.named
     */
    @JvmSynthetic
    infix fun named(name: Self.() -> String) =
        apply { this.name = name } as Self

    /**
     * Generate a dynamic [name] for this case.
     *
     * @see TestCaseIterator.named
     */
    @SinceKotlin(JAVA_ONLY)
    fun named(name: Function<Self, String>) =
        named(name::apply)

    /**
     * Use the given static [name] for this case.
     *
     * @see TestCaseIterator.named
     */
    inline infix fun named(name: String) =
        named { name }

    /** @see named */
    final override fun toString() =
        name?.invoke(this as Self) ?: name()
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase1<P1> @PublishedApi internal constructor(@JvmField val p1: P1) : TestCase<TestCase1<P1>>() {
    @JvmSynthetic override fun name() = "$p1"
    @JvmSynthetic operator fun component1() = p1
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase2<P1, P2> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2) : TestCase<TestCase2<P1, P2>>() {
    @JvmSynthetic override fun name() = "$p1, $p2"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase3<P1, P2, P3> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3) : TestCase<TestCase3<P1, P2, P3>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase4<P1, P2, P3, P4> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4) : TestCase<TestCase4<P1, P2, P3, P4>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase5<P1, P2, P3, P4, P5> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5) : TestCase<TestCase5<P1, P2, P3, P4, P5>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase6<P1, P2, P3, P4, P5, P6> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6) : TestCase<TestCase6<P1, P2, P3, P4, P5, P6>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase7<P1, P2, P3, P4, P5, P6, P7> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7) : TestCase<TestCase7<P1, P2, P3, P4, P5, P6, P7>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase8<P1, P2, P3, P4, P5, P6, P7, P8> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8) : TestCase<TestCase8<P1, P2, P3, P4, P5, P6, P7, P8>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase9<P1, P2, P3, P4, P5, P6, P7, P8, P9> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9) : TestCase<TestCase9<P1, P2, P3, P4, P5, P6, P7, P8, P9>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10) : TestCase<TestCase10<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11) : TestCase<TestCase11<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12) : TestCase<TestCase12<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13) : TestCase<TestCase13<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14) : TestCase<TestCase14<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14, @JvmField val p15: P15) : TestCase<TestCase15<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14, $p15"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
    @JvmSynthetic operator fun component15() = p15
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14, @JvmField val p15: P15, @JvmField val p16: P16) : TestCase<TestCase16<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14, $p15, $p16"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
    @JvmSynthetic operator fun component15() = p15
    @JvmSynthetic operator fun component16() = p16
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14, @JvmField val p15: P15, @JvmField val p16: P16, @JvmField val p17: P17) : TestCase<TestCase17<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14, $p15, $p16, $p17"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
    @JvmSynthetic operator fun component15() = p15
    @JvmSynthetic operator fun component16() = p16
    @JvmSynthetic operator fun component17() = p17
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14, @JvmField val p15: P15, @JvmField val p16: P16, @JvmField val p17: P17, @JvmField val p18: P18) : TestCase<TestCase18<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14, $p15, $p16, $p17, $p18"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
    @JvmSynthetic operator fun component15() = p15
    @JvmSynthetic operator fun component16() = p16
    @JvmSynthetic operator fun component17() = p17
    @JvmSynthetic operator fun component18() = p18
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14, @JvmField val p15: P15, @JvmField val p16: P16, @JvmField val p17: P17, @JvmField val p18: P18, @JvmField val p19: P19) : TestCase<TestCase19<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14, $p15, $p16, $p17, $p18, $p19"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
    @JvmSynthetic operator fun component15() = p15
    @JvmSynthetic operator fun component16() = p16
    @JvmSynthetic operator fun component17() = p17
    @JvmSynthetic operator fun component18() = p18
    @JvmSynthetic operator fun component19() = p19
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14, @JvmField val p15: P15, @JvmField val p16: P16, @JvmField val p17: P17, @JvmField val p18: P18, @JvmField val p19: P19, @JvmField val p20: P20) : TestCase<TestCase20<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14, $p15, $p16, $p17, $p18, $p19, $p20"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
    @JvmSynthetic operator fun component15() = p15
    @JvmSynthetic operator fun component16() = p16
    @JvmSynthetic operator fun component17() = p17
    @JvmSynthetic operator fun component18() = p18
    @JvmSynthetic operator fun component19() = p19
    @JvmSynthetic operator fun component20() = p20
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase21<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14, @JvmField val p15: P15, @JvmField val p16: P16, @JvmField val p17: P17, @JvmField val p18: P18, @JvmField val p19: P19, @JvmField val p20: P20, @JvmField val p21: P21) : TestCase<TestCase21<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14, $p15, $p16, $p17, $p18, $p19, $p20, $p21"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
    @JvmSynthetic operator fun component15() = p15
    @JvmSynthetic operator fun component16() = p16
    @JvmSynthetic operator fun component17() = p17
    @JvmSynthetic operator fun component18() = p18
    @JvmSynthetic operator fun component19() = p19
    @JvmSynthetic operator fun component20() = p20
    @JvmSynthetic operator fun component21() = p21
}

/** @see TestCase */
@API(status = EXPERIMENTAL, since = "5.7")
class TestCase22<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22> @PublishedApi internal constructor(@JvmField val p1: P1, @JvmField val p2: P2, @JvmField val p3: P3, @JvmField val p4: P4, @JvmField val p5: P5, @JvmField val p6: P6, @JvmField val p7: P7, @JvmField val p8: P8, @JvmField val p9: P9, @JvmField val p10: P10, @JvmField val p11: P11, @JvmField val p12: P12, @JvmField val p13: P13, @JvmField val p14: P14, @JvmField val p15: P15, @JvmField val p16: P16, @JvmField val p17: P17, @JvmField val p18: P18, @JvmField val p19: P19, @JvmField val p20: P20, @JvmField val p21: P21, @JvmField val p22: P22) : TestCase<TestCase22<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22>>() {
    @JvmSynthetic override fun name() = "$p1, $p2, $p3, $p4, $p5, $p6, $p7, $p8, $p9, $p10, $p11, $p12, $p13, $p14, $p15, $p16, $p17, $p18, $p19, $p20, $p21, $p22"
    @JvmSynthetic operator fun component1() = p1
    @JvmSynthetic operator fun component2() = p2
    @JvmSynthetic operator fun component3() = p3
    @JvmSynthetic operator fun component4() = p4
    @JvmSynthetic operator fun component5() = p5
    @JvmSynthetic operator fun component6() = p6
    @JvmSynthetic operator fun component7() = p7
    @JvmSynthetic operator fun component8() = p8
    @JvmSynthetic operator fun component9() = p9
    @JvmSynthetic operator fun component10() = p10
    @JvmSynthetic operator fun component11() = p11
    @JvmSynthetic operator fun component12() = p12
    @JvmSynthetic operator fun component13() = p13
    @JvmSynthetic operator fun component14() = p14
    @JvmSynthetic operator fun component15() = p15
    @JvmSynthetic operator fun component16() = p16
    @JvmSynthetic operator fun component17() = p17
    @JvmSynthetic operator fun component18() = p18
    @JvmSynthetic operator fun component19() = p19
    @JvmSynthetic operator fun component20() = p20
    @JvmSynthetic operator fun component21() = p21
    @JvmSynthetic operator fun component22() = p22
}

// @formatter:off
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1> case(p1: P1) = TestCase1(p1)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2> case(p1: P1, p2: P2) = TestCase2(p1, p2)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3> case(p1: P1, p2: P2, p3: P3) = TestCase3(p1, p2, p3)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4> case(p1: P1, p2: P2, p3: P3, p4: P4) = TestCase4(p1, p2, p3, p4)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5) = TestCase5(p1, p2, p3, p4, p5)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6) = TestCase6(p1, p2, p3, p4, p5, p6)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7) = TestCase7(p1, p2, p3, p4, p5, p6, p7)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8) = TestCase8(p1, p2, p3, p4, p5, p6, p7, p8)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9) = TestCase9(p1, p2, p3, p4, p5, p6, p7, p8, p9)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10) = TestCase10(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11) = TestCase11(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12) = TestCase12(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13) = TestCase13(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14) = TestCase14(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15) = TestCase15(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16) = TestCase16(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17) = TestCase17(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18) = TestCase18(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19) = TestCase19(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20) = TestCase20(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21) = TestCase21(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21)
/** @see TestCase */ @API(status = EXPERIMENTAL, since = "5.7") @JvmName("caseOf") inline fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22> case(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7, p8: P8, p9: P9, p10: P10, p11: P11, p12: P12, p13: P13, p14: P14, p15: P15, p16: P16, p17: P17, p18: P18, p19: P19, p20: P20, p21: P21, p22: P22) = TestCase22(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22)
// @formatter:on
