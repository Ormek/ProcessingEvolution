package de.my;


/**
 * Super simple cipher
 * @author Oliver Meyer
 *
 */
public class ROT {
	
	/**
	 * The String uses a Charset that cannot be processed.
	 */
	public static class InvalidCharSet extends Exception {

		public InvalidCharSet(String msg) {
			super(msg);
		}
	};

	/**
	 * Encrypt a text using ROT13. Use a send time to decrypt. Spaces are not encrypted.
	 * 
	 * @param s
	 *            text to encrypt. Maybe capitalized, but must only contain
	 *            Letters and spaces.
	 * @return a ROT13 encrypted version of the input
	 * @throws InvalidCharSet
	 *             is the String contains something else then [a-z][ A-Z].
	 */
	public static String encrypt(String s, int cipher) throws InvalidCharSet {
		StringBuffer result = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ('a' <= c && c <= 'z') {
				result.append((char)((c-'a'+cipher) %26 + 'a'));
			} else if ('A' <= c && c <= 'Z') {
				result.append((char)((c-'A'+cipher) %26 + 'A'));
			} else if (c==' ') {
				result.append(c);
			} else {
				throw new InvalidCharSet("Illegal character '" + c
						+ "' detected. Must only use [a-z][A-Z].");
			}
		}
		return result.toString();
	}
	
	public static String encrypt(String s) throws InvalidCharSet {
		return encrypt(s,13);
	}
	

	/**
	 * Decrypt a text using ROT13. This is actually the same as encrypt.
	 * 
	 * @param s
	 *            text to encrypt. Maybe capitalized, but must only contain
	 *            Letters.
	 * @return a ROT13 encrypted version of the input
	 * @throws InvalidCharSet
	 *             is the String contains something else then [a-z][A-Z].
	 */
	public static String decrypt(String s) throws InvalidCharSet {
		return encrypt(s);
	}
	
	public static String decrypt(String s, int cipher) throws InvalidCharSet {
		return encrypt(s,26-cipher%26);
	}

}
