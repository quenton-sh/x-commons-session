package x.commons.session.evictionpolicy;

import java.util.List;

/**
 * 当组内会话数目达到上限且有新会话加入时的处理策略
 * @author Quenton
 *
 */
public interface SessionEvictionPolicy {
	
	/**
	 * 选择需要被剔除的会话id
	 * @param currentSessionIds 当前组内已存在的会话id
	 * @param incomingSessionId 新加入的会话id
	 * @return 需要被剔除的会话id，null则放弃剔除
	 */
	public String getSessionIdToEvict(List<String> currentSessionIds, String incomingSessionId);
}
