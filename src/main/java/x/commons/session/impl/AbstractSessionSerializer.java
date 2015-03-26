package x.commons.session.impl;

import x.commons.session.Session;
import x.commons.session.SessionException;
import x.commons.session.SessionSerializer;

public abstract class AbstractSessionSerializer<T extends Session> implements SessionSerializer<T> {

	@Override
	public byte[] serialize(T sessionObj) {
		try {
			return this.doSerialize(sessionObj);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else {
				throw new SessionSerializeException(e);
			}
		}
	}
	
	protected abstract byte[] doSerialize(T sessionObj) throws Exception;

}
