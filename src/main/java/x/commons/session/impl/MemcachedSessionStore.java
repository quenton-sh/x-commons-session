package x.commons.session.impl;

import java.util.List;

import x.commons.memcached.MemcachedClient;
import x.commons.session.Session;
import x.commons.session.SessionDeserializer;
import x.commons.session.SessionSerializer;

/**
 * 使用Memcached存储sid<br/>
 * 此类不支持分组，所有group参数将别忽略
 * 
 * @author Quenton
 * 
 * @param <T>
 */
public class MemcachedSessionStore<T extends Session> extends AbstractSessionStore<T> {

	private MemcachedClient memcachedClient;
	private SessionSerializer<T> sessionSerializer;
	private SessionDeserializer<T> sessionDeserializer;

	public MemcachedSessionStore(MemcachedClient memcachedClient,
			SessionSerializer<T> sessionSerializer,
			SessionDeserializer<T> sessionDeserializer) {
		super();
		this.memcachedClient = memcachedClient;
		this.sessionSerializer = sessionSerializer;
		this.sessionDeserializer = sessionDeserializer;
	}

	@Override
	protected void doSet(T sessionObj, int expireSecs) throws Exception {
		byte[] data = this.sessionSerializer.serialize(sessionObj);
		this.memcachedClient.set(sessionObj.getId(), expireSecs, data);
	}

	@Override
	protected boolean doReplace(T sessionObj) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean doReplaceAndRefreshTTL(T sessionObj, int expireSecs)
			throws Exception {
		String id = sessionObj.getId();
		if (this.memcachedClient.get(id) == null) {
			return false;
		} else {
			this.doSet(sessionObj, expireSecs);
			return true;
		}
	}

	@Override
	protected boolean doRefreshTTL(String sid, int expireSecs) throws Exception {
		T obj = this.doGet(sid);
		if (obj == null) {
			return false;
		} else {
			this.doSet(obj, expireSecs);
			return true;
		}
	}

	@Override
	protected T doGet(String sid) throws Exception {
		byte[] data = this.memcachedClient.get(sid);
		return this.sessionDeserializer.deserialize(data);
	}

	@Override
	protected T doGetAndRefreshTTL(String sid, int expireSecs) throws Exception {
		T obj = this.doGet(sid);
		if (obj == null) {
			return null;
		} else {
			this.doSet(obj, expireSecs);
			return obj;
		}
	}

	@Override
	protected T doRemove(String sid) throws Exception {
		T obj = this.doGet(sid);
		if (obj != null && this.memcachedClient.delete(sid)) {
			return obj;
		}
		return null;
	}

	@Override
	protected boolean doRemoveGroup(String group) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected int doGetSessionCountInGroup(String group) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected List<String> doGetSessionIdsInGroup(String group)
			throws Exception {
		throw new UnsupportedOperationException();
	}
}
