package x.commons.session.impl;

import java.util.List;

import x.commons.session.Session;
import x.commons.session.SessionException;
import x.commons.session.SessionStore;

public abstract class AbstractSessionStore<T extends Session> implements
		SessionStore<T> {

	@Override
	public void set(T sessionObj, int expireSecs) {
		try {
			this.doSet(sessionObj, expireSecs);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}

	@Override
	public boolean replace(T sessionObj) {
		try {
			return this.doReplace(sessionObj);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}

	@Override
	public boolean replaceAndRefreshTTL(T sessionObj, int expireSecs) {
		try {
			return this.doReplaceAndRefreshTTL(sessionObj, expireSecs);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}

	@Override
	public boolean refreshTTL(String sid, int expireSecs) {
		try {
			return this.doRefreshTTL(sid, expireSecs);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}

	@Override
	public T get(String sid) {
		try {
			return this.doGet(sid);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}

	@Override
	public T remove(String sid) {
		try {
			return this.doRemove(sid);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}

	@Override
	public boolean removeGroup(String group) {
		try {
			return this.doRemoveGroup(group);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}

	@Override
	public int getSessionCountInGroup(String group) {
		try {
			return this.doGetSessionCountInGroup(group);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}

	@Override
	public List<String> getSessionIdsInGroup(String group) {
		try {
			return this.doGetSessionIdsInGroup(group);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}
	
	@Override
	public T getAndRefreshTTL(String sid, int expireSecs) {
		try {
			return this.doGetAndRefreshTTL(sid, expireSecs);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else if (e instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException) e;
			} else {
				throw new SessionStoreException(e);
			}
		}
	}
	
	protected abstract void doSet(T sessionObj, int expireSecs) throws Exception;
	
	protected abstract boolean doReplace(T sessionObj) throws Exception;
	
	protected abstract boolean doReplaceAndRefreshTTL(T sessionObj, int expireSecs) throws Exception;
	
	protected abstract boolean doRefreshTTL(String sid, int expireSecs) throws Exception;

	protected abstract T doGet(String sid) throws Exception;
	
	protected abstract T doGetAndRefreshTTL(String sid, int expireSecs) throws Exception;

	protected abstract T doRemove(String sid) throws Exception;

	protected abstract boolean doRemoveGroup(String group) throws Exception;

	protected abstract int doGetSessionCountInGroup(String group) throws Exception;

	protected abstract List<String> doGetSessionIdsInGroup(String group) throws Exception;
}
