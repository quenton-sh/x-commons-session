package x.commons.session.impl;

import x.commons.session.Session;
import x.commons.session.SessionIDGenerator;
import x.commons.session.SessionManager;
import x.commons.session.SessionStore;

public class DefaultSessionManager<T extends Session> implements SessionManager<T> {

	private int expireSecs = 900; // 默认会话有效期：15分钟
	private SessionIDGenerator sessionIDGenerator;
	private SessionStore<T> sessionStore;

	public void setExpireSecs(int expireSecs) {
		this.expireSecs = expireSecs;
	}

	public void setSessionIDGenerator(SessionIDGenerator sessionIDGenerator) {
		this.sessionIDGenerator = sessionIDGenerator;
	}

	public void setSessionStore(SessionStore<T> sessionStore) {
		this.sessionStore = sessionStore;
	}

	@Override
	public String createSession(T sessionObj) throws Exception {
		String sessionID = this.sessionIDGenerator.generate();
		sessionObj.setId(sessionID);
		this.sessionStore.put(sessionObj, expireSecs);
		return sessionID;
	}
	
	@Override
	public boolean updateSession(T sessionObj) throws Exception {
		if (sessionObj == null) {
			return false;
		}
		if (this.validateSidAndGetSessionObj(sessionObj.getId()) == null) {
			return false;
		}
		this.sessionStore.put(sessionObj, expireSecs);
		return true;
	}

	@Override
	public T validateSession(String sid) throws Exception {
		T obj = this.validateSidAndGetSessionObj(sid);
		if (obj != null) {
			// 刷新过期时间
			this.sessionStore.put(obj, expireSecs);
		}
		return obj;
	}
	
	private T validateSidAndGetSessionObj(String sid) throws Exception {
		if (sid == null || !this.sessionIDGenerator.verify(sid)) {
			return null;
		}
		return this.sessionStore.get(sid);
	}

	@Override
	public T destroySession(String sid) throws Exception {
		if (sid == null || !this.sessionIDGenerator.verify(sid)) {
			return null;
		}
		return this.sessionStore.remove(sid);
	}

}
