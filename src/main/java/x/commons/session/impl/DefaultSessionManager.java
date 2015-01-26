package x.commons.session.impl;

import java.util.HashMap;
import java.util.Map;

import x.commons.session.Session;
import x.commons.session.SessionIDGenerator;
import x.commons.session.SessionManager;
import x.commons.session.SessionStore;

public class DefaultSessionManager<T extends Session> implements SessionManager<T> {

	private int defaultSessionTimeout = 900; // 默认会话有效期：15分钟
	private Map<Integer, Integer> sessionTimeout = new HashMap<Integer, Integer>();
	private SessionIDGenerator sessionIDGenerator;
	private SessionStore<T> sessionStore;

	public void setDefaultSessionTimeout(int defaultSessionTimeout) {
		this.defaultSessionTimeout = defaultSessionTimeout;
	}

	public void setSessionTimeout(Map<Integer, Integer> sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public void setSessionIDGenerator(SessionIDGenerator sessionIDGenerator) {
		this.sessionIDGenerator = sessionIDGenerator;
	}

	public void setSessionStore(SessionStore<T> sessionStore) {
		this.sessionStore = sessionStore;
	}

	@Override
	public String createSession(T sessionObj, int type) throws Exception {
		String sessionID = this.sessionIDGenerator.generate();
		sessionObj.setId(sessionID);
		this.sessionStore.put(sessionObj, this.getSessionTimeoutForType(type));
		return sessionID;
	}
	
	@Override
	public boolean updateSession(T sessionObj, int type) throws Exception {
		if (sessionObj == null) {
			return false;
		}
		if (this.validateSidAndGetSessionObj(sessionObj.getId()) == null) {
			return false;
		}
		this.sessionStore.put(sessionObj, this.getSessionTimeoutForType(type));
		return true;
	}

	@Override
	public T validateSession(String sid, int type) throws Exception {
		T obj = this.validateSidAndGetSessionObj(sid);
		if (obj != null) {
			// 刷新过期时间
			this.sessionStore.put(obj, this.getSessionTimeoutForType(type));
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
	
	private int getSessionTimeoutForType(int type) {
		Integer sessionTimeout = this.sessionTimeout.get(type);
		if (sessionTimeout != null) {
			return sessionTimeout.intValue();
		} else {
			return this.defaultSessionTimeout;
		}
	}

}
