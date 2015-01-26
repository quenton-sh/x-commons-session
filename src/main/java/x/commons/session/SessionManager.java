package x.commons.session;

public interface SessionManager<T extends Session> {

	public String createSession(T sessionObj, int type) throws Exception;

	public boolean updateSession(T sessionObj, int type) throws Exception;

	public T validateSession(String sid, int type) throws Exception;

	public T destroySession(String sid) throws Exception;

}
