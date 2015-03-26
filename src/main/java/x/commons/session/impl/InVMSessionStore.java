package x.commons.session.impl;

import java.util.List;

import x.commons.session.Session;
import x.commons.util.cache.LRUCache;

/**
 * 使用LRU容器在JVM内存储sid<br/>
 * 此类不支持分组，所有group参数将别忽略
 * 
 * @author Quenton
 * 
 * @param <T>
 */
public class InVMSessionStore<T extends Session> extends AbstractSessionStore<T> {
	
	private final LRUCache<T> lruCache;

	public InVMSessionStore(final int size) {
		this.lruCache = new LRUCache<T>(size);
	}

	@Override
	protected void doSet(T sessionObj, int expireSecs) throws Exception {
		String id = sessionObj.getId();
		this.lruCache.set(id, expireSecs, sessionObj);
	}

	@Override
	protected boolean doReplace(T sessionObj) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean doReplaceAndRefreshTTL(T sessionObj, int expireSecs)
			throws Exception {
		String id = sessionObj.getId();
		if (this.lruCache.get(id) == null) {
			return false;
		} else {
			this.lruCache.set(id, expireSecs, sessionObj);
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
		return this.lruCache.get(sid);
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
		return this.lruCache.remove(sid);
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
