package x.commons.session;

public interface SessionManagerFactory<T extends Session> {

	public SessionManager<T> getSessionManager();
}
