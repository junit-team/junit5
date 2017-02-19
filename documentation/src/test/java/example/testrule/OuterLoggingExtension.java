package example.testrule;

public class OuterLoggingExtension extends LoggingExtension {
	
	static final String position = "outer";

	@Override
	public String getPosition() {
		return position;
	}

}
