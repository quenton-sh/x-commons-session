package x.commons.session.impl;

import java.util.List;

import x.commons.session.Session;
import x.commons.session.SessionEventCallback.SessionEventType;

public class DefaultSessionManager<T extends Session> extends AbstractSessionManager<T> {

	@Override
	protected String doCreateSession(T sessionObj, int type)
			throws Exception {
		String group = sessionObj.getGroup();
		String sid = super.sessionIDGenerator.generate();;
		int ttl = super.sessionConfig.getSessionTimeoutForType(type);
		int max = super.sessionConfig.getGroupMaxSessionNum();
		if (max > 0) { // 有组内最大会话数目限制
			if (super.sessionStore.getSessionCountInGroup(group) >= max) {
				// 组内会话数目已达上限
				List<String> currentSids = super.sessionStore.getSessionIdsInGroup(group);
				String sidToEvict = super.sessionConfig.getGroupSessionEvictionPolicy().getSessionIdToEvict(currentSids, sid);
				if (sid.equals(sidToEvict)) {
					// 拒绝创建新会话
					return null;
				} else {
					// 剔除已有会话
					super.sessionStore.remove(sidToEvict);
					if (super.sessionEventCallback != null) {
						super.sessionEventCallback.onSessionEvent(SessionEventType.SESSION_EVICTED, group, sidToEvict);
					}
				}
			}
		}
		sessionObj.setId(sid);
		super.sessionStore.set(sessionObj, ttl);
		if (super.sessionEventCallback != null) {
			super.sessionEventCallback.onSessionEvent(SessionEventType.SESSION_CREATED, group, sid);
		}
		return sid;
	}

	@Override
	protected boolean doUpdateSession(T sessionObj, int type,
			boolean refreshTTL) throws Exception {
		if (sessionObj == null) {
			return false;
		}
		String group = sessionObj.getGroup();
		if (!super.sessionIDGenerator.verify(sessionObj.getId())) {
			return false;
		}
		boolean ret = false;
		if (refreshTTL) {
			int ttl = super.sessionConfig.getSessionTimeoutForType(type);
			ret = super.sessionStore.replaceAndRefreshTTL(sessionObj, ttl);
		} else {
			ret = super.sessionStore.replace(sessionObj);
		}
		if (ret && super.sessionEventCallback != null) {
			super.sessionEventCallback.onSessionEvent(SessionEventType.SESSION_UPDATED, group, sessionObj.getId());
		}
		return ret;
	}

	@Override
	protected boolean doRefreshSessionTTL(String sid, int type)
			throws Exception {
		int ttl = super.sessionConfig.getSessionTimeoutForType(type);
		return super.sessionStore.refreshTTL(sid, ttl);
	}

	@Override
	protected T doValidateSession(String sid, int type,
			boolean refreshTTL) throws Exception {
		if (sid == null) {
			return null;
		}
		if (!super.sessionIDGenerator.verify(sid)) {
			return null;
		}
		
		T sessionObj = null;
		if (refreshTTL) {
			int ttl = super.sessionConfig.getSessionTimeoutForType(type);
			sessionObj = super.sessionStore.getAndRefreshTTL(sid, ttl);
		} else {
			sessionObj = super.sessionStore.get(sid);
		}
		
		return sessionObj;
	}

	@Override
	protected T doDestroySession(String sid) throws Exception {
		if (sid == null || !this.sessionIDGenerator.verify(sid)) {
			return null;
		}
		T ret = super.sessionStore.remove(sid);
		if (ret != null && super.sessionEventCallback != null) {
			super.sessionEventCallback.onSessionEvent(SessionEventType.SESSION_DESTROYED, ret.getGroup(), sid);
		}
		return ret;
	}

	@Override
	protected boolean doDestroyGroup(String group) throws Exception {
		boolean ret = this.sessionStore.removeGroup(group);
		if (ret && super.sessionEventCallback != null) {
			super.sessionEventCallback.onSessionEvent(SessionEventType.GROUP_DESTROYED, group, null);
		}
		return ret;
	}
}
