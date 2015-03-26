package x.commons.session.evictionpolicy;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


/**
 * 随机在组内已有会话中剔除一个
 * @author Quenton
 *
 */
public class EvictRandomPolicy implements SessionEvictionPolicy {
	
	@Override
	public String getSessionIdToEvict(List<String> currentSessionIds,
			String incomingSessionId) {
		if (currentSessionIds != null && currentSessionIds.size() > 0) {
			Random random = ThreadLocalRandom.current();
			int idx = random.nextInt(currentSessionIds.size());
			return currentSessionIds.get(idx);
		} else {
			return null;
		}
	}

}
