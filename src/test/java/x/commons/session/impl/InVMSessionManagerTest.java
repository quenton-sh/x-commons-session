package x.commons.session.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import x.commons.session.Session;
import x.commons.session.SessionManager;

public class InVMSessionManagerTest {
	
	private static InVMSessionManagerFactory<Session> factory = null;
	private static final int defaultType = 12345;
	private static final int defaultSessionTimeout = 3;
	
	private static final int type1 = 1;
	private static final int sessionTimeout1 = 1;

	private static final int type2 = 2;
	private static final int sessionTimeout2 = 2;
	
	@BeforeClass
	public static void init() {
		factory = new InVMSessionManagerFactory<Session>();
		factory.setDefaultSessionTimeout(defaultSessionTimeout);
		
		Map<Integer, Integer> timeouts = new HashMap<Integer, Integer>();
		timeouts.put(type1, sessionTimeout1);
		timeouts.put(type2, sessionTimeout2);
		factory.setSessionTimeout(timeouts);
	}

	@Test
	public void testType1() throws Exception {
		toTestForType(type1, sessionTimeout1);
	}
	
	@Test
	public void testType2() throws Exception {
		toTestForType(type2, sessionTimeout2);
	}
	
	@Test
	public void testDefault() throws Exception {
		toTestForType(defaultType, defaultSessionTimeout);
	}
	
	private void toTestForType(int type, int sessionTimeout) throws Exception {
		Session session = new Session();
		SessionManager<Session> sm = factory.getSessionManager();
		
		sessionTimeout = sessionTimeout * 1000; // s --> ms
		
		// 建立session
		String sid1 = sm.createSession(session, type);
		Session obj = sm.validateSession(sid1, type);
		assertTrue(obj != null);
		assertEquals(session.getId(), obj.getId());
		
		// 验证后刷新过期时间
		Thread.sleep(sessionTimeout - 100);
		obj = sm.validateSession(sid1, type);
		assertTrue(obj != null);
		
		Thread.sleep(sessionTimeout - 100);
		obj = sm.validateSession(sid1, type);
		assertTrue(obj != null);
		
		// session过期
		Thread.sleep(sessionTimeout);
		obj = sm.validateSession(sid1, type);
		assertTrue(obj == null);
		
		// 重新建立session
		String sid2 = sm.createSession(session, type);
		assertTrue(sid2 != null);
		assertTrue(!sid2.equals(sid1));
		obj = sm.validateSession(sid2, type);
		assertTrue(obj != null);
		assertEquals(session.getId(), obj.getId());
		
		// 删除session
		Session removedSessionObj = sm.destroySession(sid2);
		assertTrue(removedSessionObj != null && removedSessionObj.getId().equals(session.getId()));
		obj = sm.validateSession(sid2, type);
		assertTrue(obj == null);
	}
}
