package x.commons.session.evictionpolicy;

import java.util.List;


/**
 * 剔除新加入的会话id，即阻止创建新会话
 * @author Quenton
 *
 */
public class RejectNewPolicy implements SessionEvictionPolicy {

	@Override
	public String getSessionIdToEvict(List<String> currentSessionIds,
			String incomingSessionId) {
		return incomingSessionId;
	}

}
