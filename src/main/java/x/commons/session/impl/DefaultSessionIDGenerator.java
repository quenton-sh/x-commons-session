package x.commons.session.impl;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import x.commons.session.SessionIDGenerator;
import x.commons.util.HashUtils;
import x.commons.util.security.DESUtils;

public class DefaultSessionIDGenerator implements SessionIDGenerator {
	
	private static final String ENCODING = "UTF-8";
	private byte[] desKey;
	
	public DefaultSessionIDGenerator() {
		this.desKey = DESUtils.generateKey();
	}
	
	public DefaultSessionIDGenerator(byte[] desKey) {
		this.desKey = desKey;
	}

	@Override
	public String generate() throws Exception {
		String raw = UUID.randomUUID().toString().replace("-", "");
		byte[] hash = HashUtils.md5(raw.getBytes(ENCODING));
		byte[] hashCipher = DESUtils.encrypt(hash, desKey);
		String hashCipherStr = Hex.encodeHexString(hashCipher);
		return hashCipherStr.substring(0, 16) 
				+ raw
				+ hashCipherStr.substring(16, hashCipherStr.length());
	}
	
	@Override
	public boolean verify(String sid) throws Exception {
		if (sid == null || sid.length() != 80) {
			return false;
		}
		String raw = sid.substring(16, 48);
		String hashCipherStr = sid.substring(0, 16) + sid.substring(48);
		byte[] hashCipher = Hex.decodeHex(hashCipherStr.toCharArray());
		byte[] hash;
		try {
			hash = DESUtils.decrypt(hashCipher, desKey);
		} catch (Exception e) {
			return false;
		}
		byte[] rawHash = HashUtils.md5(raw.getBytes(ENCODING));
		return Arrays.equals(hash, rawHash);
	}
}
