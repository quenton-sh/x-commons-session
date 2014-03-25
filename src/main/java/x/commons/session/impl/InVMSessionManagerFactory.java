package x.commons.session.impl;

import x.commons.session.Session;
import x.commons.session.SessionIDGenerator;
import x.commons.session.SessionManager;
import x.commons.session.SessionManagerFactory;
import x.commons.session.SessionStore;

public class InVMSessionManagerFactory<T extends Session> implements SessionManagerFactory<T> {

	private int size = 100 * 10000; // 默认容积:100万
	private int expireSecs = 15 * 60; // 默认过期时间:15分钟

	public void setSize(int size) {
		this.size = size;
	}

	public void setExpireSecs(int expireSecs) {
		this.expireSecs = expireSecs;
	}

	@Override
	public SessionManager<T> getSessionManager() {
		SessionStore<T> sessionStore = new InVMSessionStore<T>(size);
		SessionIDGenerator sessionIDGenerator = new DefaultSessionIDGenerator();
		DefaultSessionManager<T> sessionManager = new DefaultSessionManager<T>();
		sessionManager.setExpireSecs(expireSecs);
		sessionManager.setSessionIDGenerator(sessionIDGenerator);
		sessionManager.setSessionStore(sessionStore);
		return sessionManager;
	}

}
