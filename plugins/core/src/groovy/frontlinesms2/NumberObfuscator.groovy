package frontlinesms2

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException;

class NumberObfuscator {
	private MessageDigest md5
	private static final int HASH_LENGTH = 5;
	private static final NumberObfuscator instance = new NumberObfuscator();
	
	public static NumberObfuscator getInstance() {
		return instance;
	}
	
	private NumberObfuscator() {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsae) {
			println "Could not retrieve MD5 MessageDigest instance."
		}
	}
	
	public String obfuscateNumber(String number) {
		md5.reset();
		md5.update(number.getBytes());
		byte[] digest = md5.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		
		return hashtext.substring(hashtext.length() - HASH_LENGTH);
	}
}
