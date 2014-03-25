package x.commons.session.impl;

import x.commons.session.Session;
import x.commons.session.SessionStore;
import x.commons.util.cache.LRUCache;


/**
 * 使用LRU容器在JVM内存储sid
 * @author Quenton
 *
 * @param <T>
 */
public class InVMSessionStore<T extends Session> implements SessionStore<T> {

	private final LRUCache<T> lruCache;
	
	public InVMSessionStore(final int size) {
		this.lruCache = new LRUCache<T>(size);
	}

	@Override
	public void put(T sessionObj, int expireSecs) {
		this.lruCache.set(sessionObj.getId(), expireSecs, sessionObj);
	}

	@Override
	public T get(String sessionID) {
		return this.lruCache.get(sessionID);
	}

	@Override
	public T remove(String sessionID) {
		return this.lruCache.remove(sessionID);
	}

}
