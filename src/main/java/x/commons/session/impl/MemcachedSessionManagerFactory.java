package x.commons.session.impl;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import x.commons.memcached.MemcachedClient;
import x.commons.session.Session;
import x.commons.session.SessionConfig;
import x.commons.session.SessionDeserializer;
import x.commons.session.SessionIDGenerator;
import x.commons.session.SessionManager;
import x.commons.session.SessionManagerFactory;
import x.commons.session.SessionSerializer;

public class MemcachedSessionManagerFactory<T extends Session> implements
		SessionManagerFactory<T> {

	private MemcachedClient memcachedClient;
	private SessionSerializer<T> sessionSerializer;
	private SessionDeserializer<T> sessionDeserializer;
	private byte[] desKey = null;
	private SessionConfig sessionConfig = new SessionConfig();

	public void setMemcachedClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
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

	@Override
	public SessionManager<T> getSessionManager() {
		MemcachedSessionStore<T> sessionStore = new MemcachedSessionStore<T>(memcachedClient,
				sessionSerializer,
				sessionDeserializer);

		SessionIDGenerator sessionIDGenerator;
		if (this.desKey != null) {
			sessionIDGenerator = new DefaultSessionIDGenerator(this.desKey);
		} else {
			sessionIDGenerator = new DefaultSessionIDGenerator();
		}

		DefaultSessionManager<T> sessionManager = new DefaultSessionManager<T>();
		sessionManager.setSessionConfig(this.sessionConfig);
		sessionManager.setSessionIDGenerator(sessionIDGenerator);
		sessionManager.setSessionStore(sessionStore);
		return sessionManager;
	}

}
