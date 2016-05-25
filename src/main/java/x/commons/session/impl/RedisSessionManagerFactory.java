package x.commons.session.impl;

import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;
import x.commons.session.Session;
import x.commons.session.SessionConfig;
import x.commons.session.SessionDeserializer;
import x.commons.session.SessionIDGenerator;
import x.commons.session.SessionManager;
import x.commons.session.SessionManagerFactory;
import x.commons.session.SessionSerializer;
import x.commons.util.Provider;

public class RedisSessionManagerFactory<T extends Session> implements
		SessionManagerFactory<T> {

	private Provider<Pool<Jedis>> jedisPoolProvider;
	private SessionSerializer<T> sessionSerializer;
	private SessionDeserializer<T> sessionDeserializer;
	private byte[] desKey; // optinal
	private SessionConfig sessionConfig; // optinal
	private String encoding; // optinal
	private String namespace; // optinal
	private int failRetryCount = 1; // 失败重试次数
	private int failRetryIntervalMillis = 1000; // 失败多次重试之间的间隔时间（毫秒） 

	public void setJedisPool(final Pool<Jedis> jedisPool) {
		this.jedisPoolProvider = new Provider<Pool<Jedis>>() {
			@Override
			public Pool<Jedis> get() {
				return jedisPool;
			}
			@Override
			public Pool<Jedis> get(Object... arg0) {
				return jedisPool;
			}
			@Override
			public Pool<Jedis> get(Map<String, Object> arg0) {
				return jedisPool;
			}
		};
	}
	
	public void setJedisPoolProvider(Provider<Pool<Jedis>> jedisPoolProvider) {
		this.jedisPoolProvider = jedisPoolProvider;
	}

	public void setSessionSerializer(SessionSerializer<T> sessionSerializer) {
		this.sessionSerializer = sessionSerializer;
	}

	public void setSessionDeserializer(
			SessionDeserializer<T> sessionDeserializer) {
		this.sessionDeserializer = sessionDeserializer;
	}

	public void setDesKey(String desKeyHex) throws DecoderException {
		this.desKey = Hex.decodeHex(desKeyHex.toCharArray());
	}

	public void setSessionConfig(SessionConfig sessionConfig) {
		this.sessionConfig = sessionConfig;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public void setFailRetryCount(int failRetryCount) {
		this.failRetryCount = failRetryCount;
	}

	public void setFailRetryIntervalMillis(int failRetryIntervalMillis) {
		this.failRetryIntervalMillis = failRetryIntervalMillis;
	}

	@Override
	public SessionManager<T> getSessionManager() {
		RedisSessionStore<T> sessionStore = new RedisSessionStore<T>(
				jedisPoolProvider,
				sessionSerializer,
				sessionDeserializer,
				encoding,
				namespace,
				failRetryCount,
				failRetryIntervalMillis);

		SessionIDGenerator sessionIDGenerator;
		if (this.desKey != null) {
			sessionIDGenerator = new DefaultSessionIDGenerator(this.desKey);
		} else {
			sessionIDGenerator = new DefaultSessionIDGenerator();
		}

		DefaultSessionManager<T> sessionManager = new DefaultSessionManager<T>();
		if (this.sessionConfig != null) {
			sessionManager.setSessionConfig(this.sessionConfig);
		}
		sessionManager.setSessionIDGenerator(sessionIDGenerator);
		sessionManager.setSessionStore(sessionStore);
		return sessionManager;
	}

}
