package x.commons.session.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import x.commons.session.Session;
import x.commons.session.SessionManager;
import x.commons.session.impl.InVMSessionManagerFactory;

public class InVMSessionManagerTest {

	@Test
	public void test() throws Exception {
		final int expireSecs = 2; // 会话有效期：2秒
		Session session = new Session();
		InVMSessionManagerFactory<Session> factory = new InVMSessionManagerFactory<Session>();
		factory.setExpireSecs(expireSecs);
		SessionManager<Session> sm = factory.getSessionManager();
		
		// 建立session
		String sid1 = sm.createSession(session);
		Session obj = sm.validateSession(sid1);
		assertTrue(obj != null);
		assertEquals(session.getId(), obj.getId());
		
		// 验证后刷新过期时间
		Thread.sleep(1800);
		obj = sm.validateSession(sid1);
		assertTrue(obj != null);
		Thread.sleep(1800);
		obj = sm.validateSession(sid1);
		assertTrue(obj != null);
		
		// session过期
		Thread.sleep(2000);
		obj = sm.validateSession(sid1);
		assertTrue(obj == null);
		
		// 重新建立session
		String sid2 = sm.createSession(session);
		assertTrue(sid2 != null);
		assertTrue(!sid2.equals(sid1));
		obj = sm.validateSession(sid2);
		assertTrue(obj != null);
		assertEquals(session.getId(), obj.getId());
		
		// 删除session
		Session removedSessionObj = sm.destroySession(sid2);
		assertTrue(removedSessionObj != null && removedSessionObj.getId().equals(session.getId()));
		obj = sm.validateSession(sid2);
		assertTrue(obj == null);
	}
}
