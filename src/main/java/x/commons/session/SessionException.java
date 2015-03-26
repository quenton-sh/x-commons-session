package x.commons.session;

@SuppressWarnings("serial")
public class SessionException extends RuntimeException {

	public SessionException() {
	}
	
	public SessionException(Throwable t) {
		super(t);
	}
	
	public SessionException(String s) {
		super(s);
	}
}
