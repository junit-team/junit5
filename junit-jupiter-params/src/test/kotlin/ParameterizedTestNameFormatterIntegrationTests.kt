import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestMethodOrder(MethodOrderer.MethodName::class)
class ParameterizedTestNameFormatterIntegrationTests {
    @BeforeEach
    fun setUp(info: TestInfo) {
        System.out.printf(
                "Class: %s, method: %s%nDisplay Name: %s%n%n",
                info.testClass.orElseThrow(),
                info.testMethod.orElseThrow(),
                info.displayName
        )
    }

    @MethodSource("methodSource")
    @ParameterizedTest
    fun `implicit'Name`(param: String) {
        Assertions.assertNotNull(param)
    }

    @MethodSource("methodSource")
    @ParameterizedTest(name = "{0}")
    fun `zero'Only`(param: String) {
        Assertions.assertNotNull(param)
    }

    @MethodSource("methodSource")
    @ParameterizedTest(name = "{displayName}")
    fun `displayName'Only`(param: String) {
        Assertions.assertNotNull(param)
    }

    @MethodSource("methodSource")
    @ParameterizedTest(name = "{displayName} - {0}")
    fun `displayName'Zero`(param: String) {
        Assertions.assertNotNull(param)
    }

    @MethodSource("methodSource")
    @ParameterizedTest(name = "{0} - {displayName}")
    fun `zero'DisplayName`(param: String) {
        Assertions.assertNotNull(param)
    }

    companion object {

        @JvmStatic
        private fun methodSource(): Array<String> =
                arrayOf(
                        "foo",
                        "bar"
                )
    }
}