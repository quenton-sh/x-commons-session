package x.commons.session;

public interface SessionDeserializer<T extends Session> {

	@SuppressWarnings("serial")
	public static class SessionDeserializeException extends SessionException {

		public SessionDeserializeException() {
			super();
		}

		public SessionDeserializeException(String s) {
			super(s);
		}

		public SessionDeserializeException(Throwable t) {
			super(t);
		}

	}

	public T deserialize(byte[] data);
}
