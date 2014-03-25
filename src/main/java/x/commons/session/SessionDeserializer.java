package x.commons.session;

public interface SessionDeserializer<T extends Session> {

	public T deserialize(byte[] data) throws Exception;
}
