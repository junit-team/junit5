package org.junit.lambda.proposal02.examples;

import org.junit.lambda.proposal02.*;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.lambda.proposal02.LambdaAssert.assertException;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

/**
 * Theories should execute without special runner (or decorator) and be mixable with standard test cases.
 *
 * The DataPoint does not seem especially useful to me, I'd rather go with generators similar to JUnit Quickcheck
 */
public class TheoryTests {

    @Theory
    public void filenameIncludesUsername1(@ForAll String username) {
        assumeFalse(username.contains("/"));
        assumeTrue(username.length() > 1);

        assertTrue(new User(username).configFileName().contains(username));
    }

    @Theory
    public void filenameIncludesUsername2(@ForAll(MyUserNames.class) String username) {
        assumeFalse(username.contains("/"));
        assumeTrue(username.length() > 1);

        assertTrue(new User(username).configFileName().contains(username));
    }


    @Theory
    public void filenameIncludesUsername3(@ForAll(MyUserNames.class) String username) {
        assumeFalse(username.contains("/"));
        assumeTrue(username.length() > 1);

        assertTrue(new User(username).configFileName().contains(username));
    }

    /**
     * A test with several parameters
     */
    @Theory
    public void allWrongPasswordCreateSameException(@ForAll(ManyUsers.class) User user, @ForAll String password, @ForAll String anotherParam) {
        assumeFalse(user.getPassword() == password);
        assertException(() -> user.validate(), RuntimeException.class);
    }

    @Test
    public void aStandardExampleBasedTestMixedIn() {
        assertEquals("johannes", new User("johannes").getUsername());
    }

}


class MyUserNames implements Generator<String> {

    @Override
    public Stream<String> generate() {
        return Stream.of("j1", "j2", "öaslkdjöalskjaöslkdfjöaslkdjfölakjsdölfkjasdölfjk", "adölkfaösldkfj/jj");
    }
}

class ManyUsers implements Generator<User> {

    @Override
    public Stream<User> generate() {
        return null;
    }
}