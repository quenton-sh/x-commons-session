package x.commons.session.evictionpolicy;

import java.util.List;


/**
 * 剔除组内已有会话中存活时间最长的
 * @author Quenton
 *
 */
public class EvictOldestPolicy implements SessionEvictionPolicy {

	@Override
	public String getSessionIdToEvict(List<String> currentSessionIds,
			String incomingSessionId) {
		if (currentSessionIds != null && currentSessionIds.size() > 0) {
			return currentSessionIds.get(0);
		} else {
			return null;
		}
	}


}
