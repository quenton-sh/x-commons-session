package x.commons.session;

public interface SessionManager<T extends Session> {

	/**
	 * 创建新会话
	 * @param sessionObj Session对象
	 * @param type 会话类型，在SessionConfig中可以针对不同类型设置不同的会话有效期
	 * @return sid，即会话id
	 */
	public String createSession(T sessionObj, int type);

	public boolean updateSession(T sessionObj, int type, boolean refreshTTL);

	public boolean refreshSessionTTL(String sid, int type);
	
	public T validateSession(String sid, int type, boolean refreshTTL);

	public T destroySession(String sid);
	
	public boolean destroyGroup(String group);
	
}
