package x.commons.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.commons.session.evictionpolicy.EvictOldestPolicy;
import x.commons.session.evictionpolicy.SessionEvictionPolicy;

public class SessionConfig {

	private int defaultSessionTimeout = 1200; // 默认会话有效期：20分钟
	private Map<Integer, Integer> sessionTimeouts = new HashMap<Integer, Integer>();
	private int groupMaxSessionNum = 0;
	private SessionEvictionPolicy groupSessionEvictionPolicy = new EvictOldestPolicy();

	/**
	 * 获取指定会话类型的超时时间（秒）；如果未针对此类型设置超时时间，则返回默认超时时间。
	 * 
	 * @param type
	 * @return
	 */
	public int getSessionTimeoutForType(int type) {
		Integer val = this.sessionTimeouts.get(type);
		return val == null ? this.defaultSessionTimeout : val.intValue();
	}

	public void setSessionTimeoutForType(int type, int timeout) {
		this.sessionTimeouts.put(type, timeout);
	}

	public int getDefaultSessionTimeout() {
		return this.defaultSessionTimeout;
	}

	public void setDefaultSessionTimeout(int timeout) {
		this.defaultSessionTimeout = timeout;
	}

	public List<Integer> getSessionTypes() {
		return new ArrayList<Integer>(this.sessionTimeouts.keySet());
	}

	public Map<Integer, Integer> getSessionTimeouts() {
		return sessionTimeouts;
	}

	/**
	 * 针对不同会话类型设置会话超时时间
	 * 
	 * @param sessionTimeouts
	 *            KEY:会话类型；VALUE:超时时间（秒）
	 */
	public void setSessionTimeouts(Map<Integer, Integer> sessionTimeouts) {
		this.sessionTimeouts = sessionTimeouts;
	}

	public int getGroupMaxSessionNum() {
		return groupMaxSessionNum;
	}

	/**
	 * 设置同一组内最多同时存在的会话数目，设为0或负数表示无限
	 * @param groupMaxSessionNum
	 */
	public void setGroupMaxSessionNum(int groupMaxSessionNum) {
		this.groupMaxSessionNum = groupMaxSessionNum;
	}

	public SessionEvictionPolicy getGroupSessionEvictionPolicy() {
		return groupSessionEvictionPolicy;
	}

	/**
	 * 设置当组内会话数目达到上限且有新会话加入时的处理策略，仅当 groupMaxSessionNum > 0 时有效<br/>
	 * 默认值：EvictionPolicyEvictOldest
	 * @param groupSessionEvictionPolicy
	 */
	public void setGroupSessionEvictionPolicy(
			SessionEvictionPolicy groupSessionEvictionPolicy) {
		this.groupSessionEvictionPolicy = groupSessionEvictionPolicy;
	}

}
