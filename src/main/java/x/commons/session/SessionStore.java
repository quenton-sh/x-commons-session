package x.commons.session;

public interface SessionStore<T extends Session> {

	public void put(T sessionObj, int expireSecs) throws Exception;
	
	public T get(String sessionID) throws Exception;
	
	public T remove(String sessionID) throws Exception;
}
