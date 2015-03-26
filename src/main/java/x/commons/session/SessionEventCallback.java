package x.commons.session;

public interface SessionEventCallback {
	
	public static enum SessionEventType {
		SESSION_CREATED,
		SESSION_UPDATED,
		SESSION_DESTROYED,
		SESSION_EVICTED,
		GROUP_DESTROYED
	}
	
	public <T> T onSessionEvent(SessionEventType et, String group, String sid);
}
