package x.commons.session.impl;

import x.commons.session.Session;
import x.commons.session.SessionConfig;
import x.commons.session.SessionManager;
import x.commons.session.SessionManagerFactory;

public class InVMSessionManagerFactory<T extends Session> implements
		SessionManagerFactory<T> {

	private int size = 10000; // 默认容积:1万
	private SessionConfig sessionConfig = new SessionConfig();	

	public void setSize(int size) {
		this.size = size;
	}

	public void setSessionConfig(SessionConfig sessionConfig) {
		this.sessionConfig = sessionConfig;
	}

	@Override
	public SessionManager<T> getSessionManager() {
		DefaultSessionManager<T> sessionManager = new DefaultSessionManager<T>();
		sessionManager.setSessionConfig(this.sessionConfig);
		sessionManager.setSessionIDGenerator(new DefaultSessionIDGenerator());
		sessionManager.setSessionStore(new InVMSessionStore<T>(this.size));
		return sessionManager;
	}

}
