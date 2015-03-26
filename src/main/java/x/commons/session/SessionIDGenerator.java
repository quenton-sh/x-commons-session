package x.commons.session;

public interface SessionIDGenerator {

	@SuppressWarnings("serial")
	public static class SessionIDException extends SessionException {

		public SessionIDException() {
			super();
		}

		public SessionIDException(String s) {
			super(s);
		}

		public SessionIDException(Throwable t) {
			super(t);
		}

	}

	public String generate();

	public boolean verify(String sid);

}
