package x.commons.session.evictionpolicy;

import java.util.List;


/**
 * 剔除组内已有会话中存活时间最短的
 * @author Quenton
 *
 */
public class EvictNewestPolicy implements SessionEvictionPolicy {

	@Override
	public String getSessionIdToEvict(List<String> currentSessionIds,
			String incomingSessionId) {
		if (currentSessionIds != null && currentSessionIds.size() > 0) {
			return currentSessionIds.get(currentSessionIds.size() - 1);
		} else {
			return null;
		}
	}


}
