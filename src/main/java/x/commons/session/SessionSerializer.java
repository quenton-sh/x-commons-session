package x.commons.session;

public interface SessionSerializer<T extends Session> {

	public byte[] serialize(T sessionObj) throws Exception;

}
