package x.commons.session.impl;

import java.util.HashMap;
import java.util.Map;

import x.commons.session.Session;
import x.commons.session.SessionIDGenerator;
import x.commons.session.SessionManager;
import x.commons.session.SessionManagerFactory;
import x.commons.session.SessionStore;

public class InVMSessionManagerFactory<T extends Session> implements
		SessionManagerFactory<T> {

	private int size = 100 * 10000; // 默认容积:100万
	private int defaultSessionTimeout = 900; // 默认会话有效期：15分钟
	private Map<Integer, Integer> sessionTimeout = new HashMap<Integer, Integer>();

	public void setSize(int size) {
		this.size = size;
	}

	public void setDefaultSessionTimeout(int secs) {
		this.defaultSessionTimeout = secs;
	}

	public void setSessionTimeout(Map<Integer, Integer> timeoutsForType) {
		this.sessionTimeout = timeoutsForType;
	}

	@Override
	public SessionManager<T> getSessionManager() {
		SessionStore<T> sessionStore = new InVMSessionStore<T>(size);
		SessionIDGenerator sessionIDGenerator = new DefaultSessionIDGenerator();
		DefaultSessionManager<T> sessionManager = new DefaultSessionManager<T>();
		sessionManager.setDefaultSessionTimeout(defaultSessionTimeout);
		sessionManager.setSessionTimeout(sessionTimeout);
		sessionManager.setSessionIDGenerator(sessionIDGenerator);
		sessionManager.setSessionStore(sessionStore);
		return sessionManager;
	}

}
