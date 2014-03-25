package x.commons.session;

public interface SessionIDGenerator {

	public String generate() throws Exception;
	
	public boolean verify(String sid) throws Exception;
	
}
