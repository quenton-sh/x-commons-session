package x.commons.session;


public interface SessionManager<T extends Session> {

	public String createSession(T sessionObj) throws Exception;
	
	public boolean updateSession(T sessionObj) throws Exception;
	
	public T validateSession(String sid) throws Exception;
	
	public T destroySession(String sid) throws Exception;
	
}
