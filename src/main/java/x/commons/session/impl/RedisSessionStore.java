package x.commons.session.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
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
			byte[] groupMemValue = this.getRedisBytes(sid);
			byte[] groupValue = this.getRedisBytes(group);
			
			jedis = this.jedisPool.getResource();
			
			Pipeline p = jedis.pipelined();
			// sid生效
			p.hset(sidKey, hashSessionValueKey, sessionValue);
			p.hset(sidKey, hashGroupKey, groupValue);
			p.expire(sidKey, expireSecs);
			// sid加入组
			long expireAt = System.currentTimeMillis() + expireSecs * 1000;
			p.zadd(groupKey, expireAt, groupMemValue);
			// 延长组有效期
			p.expire(groupKey, expireSecs);
			
			p.sync();
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
			byte[] groupMemValue = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			String groupInRedis = this.existsThenGetGroup(jedis, sidKey, groupMemValue);
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
			byte[] groupMemValue = this.getRedisBytes(sid);
			byte[] groupKey = this.buildGroupkey(group);
			byte[] groupValue = this.getRedisBytes(group);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			String groupInRedis = this.existsThenGetGroup(jedis, sidKey, groupMemValue);
			if (groupInRedis == null || !groupInRedis.equals(group)) {
				return false;
			}
			
			byte[] sessionValue = this.sessionSerializer.serialize(sessionObj);
			Pipeline p = jedis.pipelined();
			// 更新sid信息
			p.hset(sidKey, hashSessionValueKey, sessionValue);
			p.hset(sidKey, hashGroupKey, groupValue);
			p.expire(sidKey, expireSecs);
			// 更新组信息
			long expireAt = System.currentTimeMillis() + expireSecs * 1000;
			p.expire(groupKey, expireSecs);
			p.zadd(groupKey, expireAt, groupMemValue);
			
			p.sync();
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
			byte[] groupMemValue = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			String groupInRedis = this.existsThenGetGroup(jedis, sidKey, groupMemValue);
			if (groupInRedis == null) {
				return false;
			}
			byte[] groupKey = this.buildGroupkey(groupInRedis);

			Pipeline p = jedis.pipelined();
			// 更新sid信息
			p.expire(sidKey, expireSecs);
			// 更新组信息
			long expireAt = System.currentTimeMillis() + expireSecs * 1000;
			p.expire(groupKey, expireSecs);
			p.zadd(groupKey, expireAt, groupMemValue);
			
			p.sync();
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
			byte[] groupMemValue = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			SessionHashObj obj = this.existsThenGet(jedis, sidKey, groupMemValue);
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
			byte[] groupMemValue = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			SessionHashObj obj = this.existsThenGet(jedis, sidKey, groupMemValue);
			if (obj == null) {
				return null;
			}
			byte[] groupKey = this.buildGroupkey(obj.group);
			
			Pipeline p = jedis.pipelined();
			// 更新sid信息
			p.expire(sidKey, expireSecs);
			// 更新组信息
			long expireAt = System.currentTimeMillis() + expireSecs * 1000;
			p.expire(groupKey, expireSecs);
			p.zadd(groupKey, expireAt, groupMemValue);
			
			p.sync();
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
			byte[] groupMemValue = this.getRedisBytes(sid);
			
			jedis = this.jedisPool.getResource();
			
			// 检查sid、组是否存在，sid是否属于组
			SessionHashObj obj = this.existsThenGet(jedis, sidKey, groupMemValue);
			if (obj == null) {
				return null;
			}
			byte[] groupKey = this.buildGroupkey(obj.group);
			
			Pipeline p = jedis.pipelined();
			p.del(sidKey);
			p.zrem(groupKey, groupMemValue);
			p.sync();
			
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
			
			Pipeline p = jedis.pipelined();
			Response<Boolean> existsRes = p.exists(groupKey);
			p.del(groupKey);
			p.sync();
			
			return existsRes != null && existsRes.get().booleanValue();
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
			
			Pipeline p = jedis.pipelined();
			// 删除组内已过期的sid
			p.zremrangeByScore(groupKey, 0, now - 1);
			Response<Long> zcountRes = p.zcount(groupKey, now, Long.MAX_VALUE);
			p.sync();
			
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
			
			Pipeline p = jedis.pipelined();
			// 删除组内已过期的sid
			p.zremrangeByScore(groupKey, 0, now - 1);
			Response<Set<byte[]>> zrangeRes = p.zrangeByScore(groupKey, now, Long.MAX_VALUE);
			p.sync();
			
			Set<byte[]> set =  zrangeRes == null ? null : zrangeRes.get();
			if (set == null) {
				return null;
			}
			List<String> list = new ArrayList<String>(set.size());
			for (byte[] groupMemValue : set) {
				list.add(this.parseRedisBytes(groupMemValue));
			}
			return list;
		} finally {
			IOUtils.closeQuietly(jedis);
		}
	}
	
	private String existsThenGetGroup(Jedis jedis, byte[] sidKey, byte[] groupMemValue) throws Exception {
		byte[] groupBytes = jedis.hget(sidKey, this.hashGroupKey);
		if (groupBytes == null) {
			// sid不存在
			return null;
		}
		String group = this.parseRedisBytes(groupBytes);
		byte[] groupKey = this.buildGroupkey(group);
		Double d = jedis.zscore(groupKey, groupMemValue);
		if (d == null) {
			// 组信息不存在
			return null;
		}
		if (d.longValue() < System.currentTimeMillis()) {
			// 组信息已过期
			jedis.zrem(groupKey, groupMemValue);
			return null;
		}
		return group;
	}
	
	private SessionHashObj existsThenGet(Jedis jedis, byte[] sidKey, byte[] groupMemValue) throws Exception {
		Pipeline p = jedis.pipelined();
		Response<byte[]> groupRes = p.hget(sidKey, this.hashGroupKey);
		Response<byte[]> sessionValueRes = p.hget(sidKey, this.hashSessionValueKey);
		p.sync();
		if (groupRes == null || groupRes.get() == null || sessionValueRes == null || sessionValueRes.get() == null) {
			// sid不存在
			return null;
		}
		String group = this.parseRedisBytes(groupRes.get());
		byte[] groupKey = this.buildGroupkey(group);
		Double d = jedis.zscore(groupKey, groupMemValue);
		if (d == null) {
			// 组信息不存在
			return null;
		}
		if (d.longValue() < System.currentTimeMillis()) {
			// 组信息已过期
			jedis.zrem(groupKey, groupMemValue);
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
