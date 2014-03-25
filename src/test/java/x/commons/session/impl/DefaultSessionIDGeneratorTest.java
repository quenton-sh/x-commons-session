package x.commons.session.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import x.commons.session.SessionIDGenerator;
import x.commons.session.impl.DefaultSessionIDGenerator;

public class DefaultSessionIDGeneratorTest {

	@Test
	public void test() throws Exception {
		SessionIDGenerator gen = new DefaultSessionIDGenerator();
		
		// normal:
		String sid = gen.generate();
		assertTrue(sid != null && sid.length() == 80);
		boolean b = gen.verify(sid);
		assertTrue(b);
		
		// tamper：
		if (sid.endsWith("a")) {
			sid = sid.substring(0, sid.length() - 1) + "b";
		} else {
			sid = sid.substring(0, sid.length() - 1) + "a";
		}
		b = gen.verify(sid);
		assertTrue(!b);
	}
}
