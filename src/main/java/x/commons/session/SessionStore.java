package x.commons.session;

import java.util.List;

public interface SessionStore<T extends Session> {

	@SuppressWarnings("serial")
	public static class SessionStoreException extends SessionException {

		public SessionStoreException() {
			super();
		}

		public SessionStoreException(String s) {
			super(s);
		}

		public SessionStoreException(Throwable t) {
			super(t);
		}

	}

	public void set(T sessionObj, int expireSecs);
	
	public boolean replace(T sessionObj);
	
	public boolean replaceAndRefreshTTL(T sessionObj, int expireSecs);
	
	public boolean refreshTTL(String sid, int expireSecs);

	public T get(String sid);
	
	public T getAndRefreshTTL(String sid, int expireSecs);

	public T remove(String sid);

	public boolean removeGroup(String group);

	public int getSessionCountInGroup(String group);

	public List<String> getSessionIdsInGroup(String group);
	
}
