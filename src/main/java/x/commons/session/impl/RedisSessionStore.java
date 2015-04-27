package x.commons.session.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import x.commons.session.Session;
import x.commons.session.SessionDeserializer;
import x.commons.session.SessionSerializer;

/**
 * 使用Redis存储sid<br/>
 * 此类支持分组
 * 
 * @author Quenton
 * 
 * @param <T>
 */
public class RedisSessionStore<T extends Session> extends AbstractSessionStore<T> {
	
	private JedisPool jedisPool;
	private SessionSerializer<T> sessionSerializer;
	private SessionDeserializer<T> sessionDeserializer;
	private String encoding = "UTF-8";
	private String namespace = "_xcsr_";
	
	private final byte[] hashSessionValueKey;
	private final byte[] hashGroupKey;
	
	public RedisSessionStore(JedisPool jedisPool,
			SessionSerializer<T> sessionSerializer,
			SessionDeserializer<T> sessionDeserializer, String encoding,
			String namespace) {
		super();
		this.jedisPool = jedisPool;
		this.sessionSerializer = sessionSerializer;
		this.sessionDeserializer = sessionDeserializer;
		if (encoding != null) {
			this.encoding = encoding;
		}
		if (namespace != null) {
			this.namespace = namespace;
		}
		
		try {
			this.hashSessionValueKey = this.getRedisBytes("v");
			this.hashGroupKey = this.getRedisBytes("g");
		} catch (UnsupportedEncodingException e) {
			throw new SessionStoreException(e);
		}
	}

	private byte[] buildSessionIdKey(String sid) throws UnsupportedEncodingException {
		String key = this.namespace + "sid:" + sid;
		return key.getBytes(this.encoding);
	}
	
	private byte[] buildGroupkey(String group) throws UnsupportedEncodingException {
		String key = this.namespace + "g:" + group;
		return key.getBytes(this.encoding);
	}
	
	private byte[] getRedisBytes(String val) throws UnsupportedEncodingException {
		return val.getBytes(this.encoding);
	}
	
	private String parseRedisBytes(byte[] val) throws UnsupportedEncodingException {
		return new String(val, this.encoding);
	}

	@Override
	protected void doSet(T sessionObj, int expireSecs)
			throws Exception {
		Jedis jedis = null;
		try {
			String sid = sessionObj.getId();
			String group = sessionObj.getGroup();
			
			byte[] sidKey = this.buildSessionIdKey(sessionObj.getId());
			byte[] sessionValue = this.sessionSerializer.serialize(sessionObj);
			byte[] groupKey = this.buildGroupkey(group);
			byte[] sidValueInGroup = this.getRedisBytes(sid);
			byte[] groupValue = this.getRedisBytes(group);
			
			jedis = this.jedisPool.getResource();
			
			Transaction t = jedis.multi();
			// sid生效
			t.hset(sidKey, hashSessionValueKey, sessionValue);
			t.hset(sidKey, hashGroupKey, groupValue);
			t.expire(sidKey, expireSecs);
			
			// 删除组内已过期的sid
			t.zremrangeByScore(groupKey, 0, System.currentTimeMillis() - 1);
			
			// sid加入组
			long expireAt = System.currentTimeMillis() + expireSecs * 1000;
			t.zadd(groupKey, expireAt, sidValueInGroup);
			
			// 延长组有效期
			t.expire(groupKey, expireSecs);
			
			t.exec();
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}

	@Override
	protected boolean doReplace(T sessionObj) throws Exception {
		Jedis jedis = null;
		try {
			String sid = sessionObj.getId();
			String group = sessionObj.getGroup();
			
			byte[] sidKey = this.buildSessionIdKey(sessionObj.getId());
			byte[] sidValueInGroup = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			String groupInRedis = this.existsThenGetGroup(jedis, sidKey, sidValueInGroup);
			if (groupInRedis == null || !groupInRedis.equals(group)) {
				return false;
			}
			
			byte[] sessionValue = this.sessionSerializer.serialize(sessionObj);
			// 更新sid信息
			jedis.hset(sidKey, this.hashSessionValueKey, sessionValue);
			return true;
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}

	@Override
	protected boolean doReplaceAndRefreshTTL(T sessionObj, int expireSecs)
			throws Exception {
		Jedis jedis = null;
		try {
			String sid = sessionObj.getId();
			String group = sessionObj.getGroup();
			
			byte[] sidKey = this.buildSessionIdKey(sessionObj.getId());
			byte[] sidValueInGroup = this.getRedisBytes(sid);
			byte[] groupKey = this.buildGroupkey(group);
			byte[] groupValue = this.getRedisBytes(group);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			String groupInRedis = this.existsThenGetGroup(jedis, sidKey, sidValueInGroup);
			if (groupInRedis == null || !groupInRedis.equals(group)) {
				return false;
			}
			
			byte[] sessionValue = this.sessionSerializer.serialize(sessionObj);
			Transaction t = jedis.multi();
			// 更新sid信息
			t.hset(sidKey, hashSessionValueKey, sessionValue);
			t.hset(sidKey, hashGroupKey, groupValue);
			t.expire(sidKey, expireSecs);
			// 更新组信息
			long expireAt = System.currentTimeMillis() + expireSecs * 1000;
			t.expire(groupKey, expireSecs);
			t.zadd(groupKey, expireAt, sidValueInGroup);
			
			t.exec();
			return true;
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}

	@Override
	protected boolean doRefreshTTL(String sid, int expireSecs) throws Exception {
		Jedis jedis = null;
		try {
			byte[] sidKey = this.buildSessionIdKey(sid);
			byte[] sidValueInGroup = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			String groupInRedis = this.existsThenGetGroup(jedis, sidKey, sidValueInGroup);
			if (groupInRedis == null) {
				return false;
			}
			byte[] groupKey = this.buildGroupkey(groupInRedis);

			Transaction t = jedis.multi();
			// 更新sid信息
			t.expire(sidKey, expireSecs);
			// 更新组信息
			long expireAt = System.currentTimeMillis() + expireSecs * 1000;
			t.expire(groupKey, expireSecs);
			t.zadd(groupKey, expireAt, sidValueInGroup);
			
			t.exec();
			return true;
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}

	@Override
	protected T doGet(String sid) throws Exception {
		Jedis jedis = null;
		try {
			byte[] sidKey = this.buildSessionIdKey(sid);
			byte[] sidValueInGroup = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			SessionHashObj obj = this.existsThenGet(jedis, sidKey, sidValueInGroup);
			if (obj == null) {
				return null;
			}
			return this.sessionDeserializer.deserialize(obj.sessionValue);
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}
	
	@Override
	protected T doGetAndRefreshTTL(String sid, int expireSecs) throws Exception {
		Jedis jedis = null;
		try {
			byte[] sidKey = this.buildSessionIdKey(sid);
			byte[] sidValueInGroup = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			SessionHashObj obj = this.existsThenGet(jedis, sidKey, sidValueInGroup);
			if (obj == null) {
				return null;
			}
			byte[] groupKey = this.buildGroupkey(obj.group);
			
			Transaction t = jedis.multi();
			// 更新sid信息
			t.expire(sidKey, expireSecs);
			// 更新组信息
			long expireAt = System.currentTimeMillis() + expireSecs * 1000;
			t.expire(groupKey, expireSecs);
			t.zadd(groupKey, expireAt, sidValueInGroup);
			
			t.exec();
			return this.sessionDeserializer.deserialize(obj.sessionValue);
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}

	@Override
	protected T doRemove(String sid) throws Exception {
		Jedis jedis = null;
		try {
			byte[] sidKey = this.buildSessionIdKey(sid);
			byte[] sidValueInGroup = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			SessionHashObj obj = this.existsThenGet(jedis, sidKey, sidValueInGroup);
			if (obj == null) {
				return null;
			}
			byte[] groupKey = this.buildGroupkey(obj.group);
			
			Transaction t = jedis.multi();
			t.del(sidKey); // 删除sid（使会话失效）
			t.zrem(groupKey, sidValueInGroup); // 从组中删除sid
			t.exec();
			
			return this.sessionDeserializer.deserialize(obj.sessionValue);
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}

	@Override
	protected boolean doRemoveGroup(String group) throws Exception {
		Jedis jedis = null;
		try {
			byte[] groupKey = this.buildGroupkey(group);
			jedis = this.jedisPool.getResource();
			
			Long l = jedis.del(groupKey);
			
			return l != null && l.intValue() > 0;
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}

	@Override
	protected int doGetSessionCountInGroup(String group) throws Exception {
		Jedis jedis = null;
		try {
			long now = System.currentTimeMillis();
			byte[] groupKey = this.buildGroupkey(group);
			
			jedis = this.jedisPool.getResource();
			
			Transaction t = jedis.multi();
			// 删除组内已过期的sid
			t.zremrangeByScore(groupKey, 0, now - 1);
			// 获取组内未过期的sid数量
			Response<Long> zcountRes = t.zcount(groupKey, now, Long.MAX_VALUE);
			t.exec();
			
			return zcountRes == null ? 0 : zcountRes.get().intValue();
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}

	@Override
	protected List<String> doGetSessionIdsInGroup(String group)
			throws Exception {
		Jedis jedis = null;
		try {
			long now = System.currentTimeMillis();
			byte[] groupKey = this.buildGroupkey(group);
			
			jedis = this.jedisPool.getResource();
			
			Transaction t = jedis.multi();
			// 删除组内已过期的sid
			t.zremrangeByScore(groupKey, 0, now - 1);
			// 获取组内未过期的sid集合
			Response<Set<byte[]>> zrangeRes = t.zrangeByScore(groupKey, now, Long.MAX_VALUE);
			t.exec();
			
			Set<byte[]> set =  zrangeRes == null ? null : zrangeRes.get();
			if (set == null) {
				return null;
			}
			List<String> list = new ArrayList<String>(set.size());
			for (byte[] sidValueInGroup : set) {
				list.add(this.parseRedisBytes(sidValueInGroup));
			}
			return list;
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}
	
	private String existsThenGetGroup(Jedis jedis, byte[] sidKey, byte[] sidValueInGroup) throws Exception {
		byte[] groupBytes = jedis.hget(sidKey, this.hashGroupKey);
		if (groupBytes == null) {
			// sid不存在
			return null;
		}
		String group = this.parseRedisBytes(groupBytes);
		byte[] groupKey = this.buildGroupkey(group);
		Double d = jedis.zscore(groupKey, sidValueInGroup);
		if (d == null) {
			// 组信息不存在
			return null;
		}
		if (d.longValue() < System.currentTimeMillis()) {
			// 组内当前sid信息已过期
			jedis.zrem(groupKey, sidValueInGroup);
			return null;
		}
		return group;
	}
	
	private SessionHashObj existsThenGet(Jedis jedis, byte[] sidKey, byte[] sidValueInGroup) throws Exception {
		Transaction t = jedis.multi();
		Response<byte[]> groupRes = t.hget(sidKey, this.hashGroupKey);
		Response<byte[]> sessionValueRes = t.hget(sidKey, this.hashSessionValueKey);
		t.exec();
		if (groupRes == null || groupRes.get() == null || sessionValueRes == null || sessionValueRes.get() == null) {
			// sid不存在
			return null;
		}
		String group = this.parseRedisBytes(groupRes.get());
		byte[] groupKey = this.buildGroupkey(group);
		Double d = jedis.zscore(groupKey, sidValueInGroup);
		if (d == null) {
			// 组信息不存在
			return null;
		}
		if (d.longValue() < System.currentTimeMillis()) {
			// 组内当前sid信息已过期
			jedis.zrem(groupKey, sidValueInGroup);
			return null;
		}
		return new SessionHashObj(group, sessionValueRes.get());
	}
	
	private static class SessionHashObj {
		private final String group;
		private final byte[] sessionValue;
		SessionHashObj(String group, byte[] sessionValue) {
			this.group = group;
			this.sessionValue = sessionValue;
		}
	}
	
}
