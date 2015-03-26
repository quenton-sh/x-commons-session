package x.commons.session.impl;

import x.commons.session.Session;
import x.commons.session.SessionConfig;
import x.commons.session.SessionEventCallback;
import x.commons.session.SessionException;
import x.commons.session.SessionIDGenerator;
import x.commons.session.SessionManager;
import x.commons.session.SessionStore;

public abstract class AbstractSessionManager<T extends Session> implements SessionManager<T> {
	
	protected SessionConfig sessionConfig = new SessionConfig();
	protected SessionIDGenerator sessionIDGenerator;
	protected SessionStore<T> sessionStore;
	protected SessionEventCallback sessionEventCallback;
	
	public SessionConfig getSessionConfig() {
		return sessionConfig;
	}

	public void setSessionConfig(SessionConfig sessionConfig) {
		this.sessionConfig = sessionConfig;
	}

	public SessionIDGenerator getSessionIDGenerator() {
		return sessionIDGenerator;
	}

	public void setSessionIDGenerator(SessionIDGenerator sessionIDGenerator) {
		this.sessionIDGenerator = sessionIDGenerator;
	}

	public SessionStore<T> getSessionStore() {
		return sessionStore;
	}

	public void setSessionStore(SessionStore<T> sessionStore) {
		this.sessionStore = sessionStore;
	}

	public SessionEventCallback getSessionEventCallback() {
		return sessionEventCallback;
	}

	public void setSessionEventCallback(SessionEventCallback sessionEventCallback) {
		this.sessionEventCallback = sessionEventCallback;
	}

	@Override
	public String createSession(T sessionObj, int type) {
		try {
			return this.doCreateSession(sessionObj, type);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionException(e);
			}
		}
	}

	@Override
	public boolean updateSession(T sessionObj, int type, boolean refreshTTL) {
		try {
			return this.doUpdateSession(sessionObj, type, refreshTTL);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionException(e);
			}
		}
	}

	@Override
	public boolean refreshSessionTTL(String sid, int type) {
		try {
			return this.doRefreshSessionTTL(sid, type);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionException(e);
			}
		}
	}
	
	@Override
	public T validateSession(String sid, int type, boolean refreshTTL) {
		try {
			return this.doValidateSession(sid, type, refreshTTL);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionException(e);
			}
		}
	}

	@Override
	public T destroySession(String sid) {
		try {
			return this.doDestroySession(sid);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionException(e);
			}
		}
	}
	
	@Override
	public boolean destroyGroup(String group) {
		try {
			return this.doDestroyGroup(group);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionException(e);
			}
		}
	}
	
	protected abstract String doCreateSession(T sessionObj, int type) throws Exception;
	
	protected abstract boolean doUpdateSession(T sessionObj, int type, boolean refreshTTL) throws Exception;

	protected abstract boolean doRefreshSessionTTL(String sid, int type) throws Exception;
	
	protected abstract T doValidateSession(String sid, int type, boolean refreshTTL) throws Exception;

	protected abstract T doDestroySession(String sid) throws Exception;
	
	protected abstract boolean doDestroyGroup(String group) throws Exception;

}
