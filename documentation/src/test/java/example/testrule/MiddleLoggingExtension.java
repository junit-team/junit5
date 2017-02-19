package example.testrule;

public class MiddleLoggingExtension extends LoggingExtension {
	
	static final String position = "middle";

	@Override
	public String getPosition() {
		return position;
	}

}
