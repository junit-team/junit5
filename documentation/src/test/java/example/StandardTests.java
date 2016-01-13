package example;


// tag::user_guide[]
import org.junit.gen5.api.*;

class StandardTests {

	@BeforeAll
	static void initAll() {}

	@BeforeEach
	void init() {}

	@Test
	void succeedingTest() {}

	@AfterEach
	void tearDown() {}

	@AfterAll
	static void tearDownAll() {}

}
// end::user_guide[]
