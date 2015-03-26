package x.commons.session;

public interface SessionSerializer<T extends Session> {
	
	@SuppressWarnings("serial")
	public static class SessionSerializeException extends SessionException {

		public SessionSerializeException() {
			super();
		}

		public SessionSerializeException(String s) {
			super(s);
		}

		public SessionSerializeException(Throwable t) {
			super(t);
		}

	}

	public byte[] serialize(T sessionObj);

}
