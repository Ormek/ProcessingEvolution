package de.my;

import java.util.EnumMap;
import java.util.Random;

import de.my.ROT.InvalidCharSet;

/**
 * A suitable assignment of presents.
 * 
 * @author Oliver Meyer
 * 
 */
public class GivingOrder {

	static final int PERSON_COUNT = Person.values().length;

	/**
	 * Create a new Giving Order, where no gifts are given.
	 */
	public GivingOrder() {
		ALLOWED = init();
	}

	/**
	 * The set of persons we care about.
	 */
	public static enum Person {
		Simone, Oliver, Andrea, Markus, Susanne, Martin, Vera, Jochen,
	}

	/**
	 * You are trying to give a present e.g. to the spouse of the giver.
	 * 
	 * @author Oliver Meyer
	 * 
	 */
	public class InvalidPresent extends Exception {

		private static final long serialVersionUID = 1L;

		public InvalidPresent(String arg0) {
			super(arg0);
		}
	}

	/**
	 * That Person already receives a presents
	 * 
	 * @author Oliver Meyer
	 * 
	 */
	public class DuplicatePresent extends Exception implements Cloneable {

		private static final long serialVersionUID = 1L;

		public DuplicatePresent(String message) {
			super(message);
		}
	};

	/**
	 * Array to hold who is giving a present to whom. Iff receiver.get(from)=to,
	 * then from is giving a present to to.
	 */
	private EnumMap<Person, Person> receiver = new EnumMap<Person, Person>(Person.class);

	/**
	 * In this giving Order Person 'from' gives a present to 'to'. This will
	 * automatically switch who 'from' is giving a present to, but it will not
	 * change other assignments.
	 * 
	 * @param from
	 *            The person giving the present.
	 * @param to
	 *            The person eventually receiving the present.
	 * @throws InvalidPresent
	 *             If from may not gift person to, e. g. to is the spouse of
	 *             from.
	 * @throws DuplicatePresent
	 *             If from is already receiving a present from someone else.
	 */
	public void give(Person from, Person to) throws InvalidPresent, DuplicatePresent {
		if (!ALLOWED[from.ordinal()][to.ordinal()]) {
			throw new InvalidPresent(
					"Person " + from.toString() + " must not give a present to Person " + to.toString() + ".");
		}
		// Check for duplicates
		// If 'to' is already receiving a present from someone other than 'from'
		// it is a duplicate
		for (Person giver : receiver.keySet()) {
			if (receiver.get(giver) == to && giver != from) {
				throw new DuplicatePresent("Person " + to + " already receives a present from " + giver
						+ ". You are trying to give him a second present from " + from + ". That is not allowed.");
			}

		}
		receiver.put(from, to);
	}

	/**
	 * This array stores who is allowed to give a present to whom. Iff
	 * ALLOWED[from][to] the 'from' may give a present to 'to'. These are the
	 * overall rules and do not reflect the current state of the object.
	 */
	protected final boolean[][] ALLOWED;

	protected boolean[][] init() {
		boolean value;
		boolean[][] result = new boolean[PERSON_COUNT][PERSON_COUNT];
		for (Person from : Person.values()) {
			for (Person to : Person.values()) {
				if (from == to) {
					value = false;
				} else {
					switch (from) {
					case Andrea:
						// Ist mit Markus ein Paar
						switch (to) {
						case Markus: // <3
						case Susanne: // 2016
						case Vera: // 2017
							value = false;
							break;
						default:
							value = true;
						}
						break;
					case Jochen:
						// Ist mit Vera verheiratet
						switch (to) {
						case Susanne: // 2015
						case Andrea: // 2016
						case Oliver: // 2017
						case Vera: // <3
							value = false;
							break;
						default:
							value = true;
						}
						break;
					case Markus:
						// Ist mit Andrea ein Paar
						switch (to) {
						case Andrea: // <3
						case Jochen: // 2015
						case Vera: // 2016
						case Susanne: // 2017
							value = false;
							break;
						default:
							value = true;
						}
						break;
					case Martin:
						// Ist mit Susanne verheiratet
						switch (to) {
						case Simone: // 2015
						case Oliver: // 2016
						case Jochen: // 2017
						case Susanne: // <3
							value = false;
							break;
						default:
							value = true;
						}
						break;
					case Oliver:
						// Liebt Simone
						switch (to) {
						case Martin: // 2017
						case Jochen: // 2016
						case Markus: // 2015
						case Simone: // <3
							value = false;
							break;
						default:
							value = true;
						}
						break;
					case Simone:
						// Ist mit Oliver ein Paar
						switch (to) {
						case Vera: // 2015
						case Martin: // 2016
						case Andrea: // 2017
						case Oliver: // <3
							value = false;
							break;
						default:
							value = true;
						}
						break;
					case Susanne:
						// Ist mit Martin verheiratet
						switch (to) {
						case Simone: // 2017
						case Markus: // 2016
						case Oliver: // 2015
						case Martin: // <3
							value = false;
							break;
						default:
							value = true;
						}
						break;
					case Vera:
						// Ist mit Jochen verheiratet
						switch (to) {
						case Markus: // 2017
						case Simone: // 2016
						case Martin: // 2015
						case Jochen: // <3
							value = false;
							break;
						default:
							value = true;
						}
						break;
					default:
						throw new RuntimeException("Array init is crap.");
					}
				}
				result[from.ordinal()][to.ordinal()] = value;
			}
		}
		return result;
	}

	boolean isComplete() {
		boolean result = receiver.size() == PERSON_COUNT;
		return result;
	}

	/**
	 * Create a multiline representation of the object.
	 * 
	 * @return A string containing line breaks. You may use printLn to the print
	 *         the String.
	 */
	public String print() {
		StringBuffer result = new StringBuffer();
		for (Person p : receiver.keySet()) {
			result.append(p.toString());
			result.append("->");
			result.append(receiver.get(p));
			result.append("\n");
		}
		return result.toString();
	}

	@Override
	protected GivingOrder clone() throws CloneNotSupportedException {
		GivingOrder clone = new GivingOrder();
		clone.receiver = new EnumMap<Person, Person>(this.receiver);
		return clone;
	}

	/**
	 * @return part of a semikolon seperated line of receivers
	 */
	public String printInCSV() {
		StringBuffer result = new StringBuffer();
		for (Person p : Person.values()) {
			if (receiver.containsKey(p)) {
				result.append(receiver.get(p));
			}
			result.append(";");
		}
		return result.toString();
	}

	public String printAllowed() {
		StringBuffer result = new StringBuffer();
		result.append("\\ ");
		for (Person to : Person.values()) {
			result.append(to.toString().charAt(0));
			result.append(' ');
		}
		result.append("\n");
		for (Person from : Person.values()) {
			result.append(from.toString().charAt(0));
			result.append(' ');
			for (Person to : Person.values()) {
				result.append(ALLOWED[from.ordinal()][to.ordinal()] ? "X " : "  ");
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * @return semikolon seperated Line of givers (Constant)
	 */
	public static String headCSV() {
		StringBuffer result = new StringBuffer();
		for (Person p : Person.values()) {
			result.append(p.toString());
			result.append(";");
		}
		return result.toString();

	}

	/**
	 * Output the name of the giver, a rotation number to use to decipher the
	 * receiver and the encrypted receiver name.
	 * 
	 * @throws InvalidCharSet
	 *             If the enum values contain invalid characters.
	 * @deprecated Use #printEncrypted instead
	 */
	public String printEncryptedOldLink() throws InvalidCharSet {
		StringBuffer result = new StringBuffer();
		Random generator = new Random();
		for (Person p : receiver.keySet()) {
			String giftReceiver = receiver.get(p).toString();
			int cipher = generator.nextInt(25) + 1;
			result.append("Link für " + p.toString() + ": http://web.forret.com/tools/rot13.asp?rot=" + cipher
					+ "&clear=" + spaceToPlus(
							ROT.encrypt(p.toString() + " Du beschenkst in diesem Jahr " + giftReceiver, 26 - cipher)));
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Output the name of the giver, a rotation number to use to decipher the
	 * receiver and the encrypted receiver name.
	 * 
	 * @throws InvalidCharSet
	 *             If the enum values contain invalid characters.
	 */
	public String printEncrypted() throws InvalidCharSet {
		StringBuffer result = new StringBuffer();
		Random generator = new Random();
		for (Person p : receiver.keySet()) {
			String giftReceiver = receiver.get(p).toString();
			int cipher = generator.nextInt(25) + 1;
			result.append("Link für " + p.toString() + ": ROT-" + cipher + " auf: http://theblob.org/rot.cgi?text="
					+ spaceToPercent20(
							ROT.encrypt(p.toString() + " Du beschenkst in diesem Jahr " + giftReceiver, 26 - cipher)));
			result.append("\n");
		}
		return result.toString();
	}

	private String spaceToPlus(String string) {
		return string.replace(' ', '+');
	}

	private String spaceToPercent20(String string) {
		return string.replace(" ", "%20");
	}

}
