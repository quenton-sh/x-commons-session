package x.commons.session.impl;

import x.commons.memcached.MemcachedClient;
import x.commons.session.Session;
import x.commons.session.SessionDeserializer;
import x.commons.session.SessionSerializer;
import x.commons.session.SessionStore;

/**
 * 使用Memcached存储sid
 * 
 * @author Quenton
 * 
 * @param <T>
 */
public class MemcachedSessionStore<T extends Session> implements SessionStore<T> {

	private MemcachedClient memcachedClient;
	private SessionSerializer<T> sessionSerializer;
	private SessionDeserializer<T> sessionDeserializer;

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

	@Override
	public void put(T sessionObj, int expireSecs)
			throws Exception {
		if (this.sessionSerializer != null) {
			byte[] data = this.sessionSerializer.serialize(sessionObj);
			this.memcachedClient.set(sessionObj.getId(), expireSecs, data);
		} else {
			this.memcachedClient.set(sessionObj.getId(), expireSecs, sessionObj);
		}
	}

	@Override
	public T get(String sessionID) throws Exception {
		if (this.sessionDeserializer != null) {
			byte[] data = this.memcachedClient.get(sessionID);
			return this.sessionDeserializer.deserialize(data);
		} else {
			return this.memcachedClient.get(sessionID);
		}
	}

	@Override
	public T remove(String sessionID) throws Exception {
		T obj = this.get(sessionID);
		if (obj != null && this.memcachedClient.delete(sessionID)) {
			return obj;
		}
		return null;
	}

}
