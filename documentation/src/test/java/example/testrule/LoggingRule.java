package example.testrule;

import java.util.logging.Logger;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class LoggingRule implements TestRule {

//	private static final Logger logger = Logger.getLogger("");
	
	String position;
	
	public LoggingRule(String position)
	{
		this.position = position;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
//				logger.info("starting " + position);
				base.evaluate();
//				logger.info("finished " + position);
			}
			
		};
		
	}

}
