package x.commons.session.impl;

import x.commons.session.Session;
import x.commons.session.SessionDeserializer;
import x.commons.session.SessionException;

public abstract class AbstractSessionDeserializer<T extends Session> implements SessionDeserializer<T>  {

	@Override
	public T deserialize(byte[] data) {
		try {
			return this.doDeserialize(data);
		} catch (Exception e) {
			if (e instanceof SessionException) {
				throw (SessionException) e;
			} else {
				throw new SessionDeserializeException(e);
			}
		}
	}
	
	protected abstract T doDeserialize(byte[] data) throws Exception;

}
