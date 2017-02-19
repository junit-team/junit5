package example.testrule;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class RuleChainVintageDemo {
	
    @Rule
    public RuleChain chain= RuleChain
                           .outerRule(new LoggingRule("outer rule"))
                           .around(new LoggingRule("middle rule"))
                           .around(new LoggingRule("inner rule"));

    @Test
    public void example() {
            assertTrue(true);
//            System.out.println("example ran");
 }
    
}
