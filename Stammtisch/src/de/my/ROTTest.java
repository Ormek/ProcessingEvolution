package de.my;

import de.my.ROT.InvalidCharSet;
import junit.framework.TestCase;

public class ROTTest extends TestCase {

	public void testEncrypt() throws InvalidCharSet {
		try {
			ROT.encrypt("Olvier,");
			fail("No Exception, although expected.");
		} catch (InvalidCharSet e) {
		}
		assertEquals("Byvire", ROT.encrypt("Oliver"));
		assertEquals("abcdefghijklmnopqrstuvwxyz", ROT.decrypt(ROT
				.encrypt("abcdefghijklmnopqrstuvwxyz")));
		assertEquals("ABCDEFGHI jklmnopqrstuvwxyz JKLMNOPQRSTUVWXYZ", ROT.decrypt(ROT
				.encrypt("ABCDEFGHI jklmnopqrstuvwxyz JKLMNOPQRSTUVWXYZ")));
		assertEquals("ABCDEFGHIJKLMNOPQRSTUVW", ROT.encrypt(ROT.encrypt("ABCDEFGHIJKLMNOPQRSTUVW", 12),14));
		assertEquals("ABCDEFGHIJKLMNOPQRSTUVW", ROT.encrypt(ROT.encrypt("ABCDEFGHIJKLMNOPQRSTUVW", 26),0));
		assertEquals("ABCDEFGHIJKLMNOPQRSTUVW", ROT.encrypt(ROT.encrypt("ABCDEFGHIJKLMNOPQRSTUVW", 27),25));
	}

	public void testDecrypt() throws InvalidCharSet {
		assertEquals("Oliver", ROT.decrypt("Byvire"));
		assertEquals("ABCDEFGHIJKLMNOPQRSTUVW", ROT.decrypt(ROT.encrypt("ABCDEFGHIJKLMNOPQRSTUVW", 12),12));
	}
	
}
