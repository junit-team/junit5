package org.junit.vintage.engine.samples.spock

import spock.lang.Specification
import spock.lang.Unroll

class SpockTestCaseWithUnrolledAndRegularFeatureMethods extends Specification {

    @Unroll
    def "unrolled feature for #input"() {
        expect:
        input == 42
        where:
        input << [23, 42]
    }

    def "regular"() {
        expect:
        true
    }
}
