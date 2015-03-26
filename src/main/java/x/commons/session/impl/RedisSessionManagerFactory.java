package x.commons.session.impl;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import redis.clients.jedis.JedisPool;
import x.commons.session.Session;
import x.commons.session.SessionConfig;
import x.commons.session.SessionDeserializer;
import x.commons.session.SessionIDGenerator;
import x.commons.session.SessionManager;
import x.commons.session.SessionManagerFactory;
import x.commons.session.SessionSerializer;

public class RedisSessionManagerFactory<T extends Session> implements
		SessionManagerFactory<T> {

	private JedisPool jedisPool;
	private SessionSerializer<T> sessionSerializer;
	private SessionDeserializer<T> sessionDeserializer;
	private byte[] desKey; // optinal
	private SessionConfig sessionConfig; // optinal
	private String encoding; // optinal
	private String namespace; // optinal

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
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

	@Override
	public SessionManager<T> getSessionManager() {
		RedisSessionStore<T> sessionStore = new RedisSessionStore<T>(
				jedisPool,
				sessionSerializer,
				sessionDeserializer,
				encoding,
				namespace);

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
