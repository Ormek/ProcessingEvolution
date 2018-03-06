package de.my;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;

import static de.my.GivingOrder.Person;

/**
 * This class knows when a Persons birthday is and my return it as a formatted
 * text.
 * 
 * @author Oliver Meyer
 * 
 */
public class BirthDates {
	static private final EnumMap<Person, Date> birthdays = new EnumMap<GivingOrder.Person, Date>(
			Person.class);

	static {
		DateFormat df = new SimpleDateFormat("d.M.");
		try {
			birthdays.put(Person.Oliver, df.parse("15.12."));
			birthdays.put(Person.Simone, df.parse("1.1."));
			birthdays.put(Person.Vera, df.parse("16.2."));
			birthdays.put(Person.Markus, df.parse("13.3."));
			birthdays.put(Person.Susanne, df.parse("10.6."));
			birthdays.put(Person.Jochen, df.parse("9.9."));
			birthdays.put(Person.Martin, df.parse("27.10."));
			birthdays.put(Person.Andrea, df.parse("4.6."));
		} catch (ParseException e) {
			System.err
					.println("Cannot define a birth date! Expect a Null pointer exception and abort");
		}

	}

	public static void printAllBirthdays() {

		DateFormat df = new SimpleDateFormat("d. MMMM");
		for (Person p : Person.values()) {
			System.out.println("Der Geburtstag von " + p.toString()
					+ " ist am " + df.format(birthdays.get(p))+".");
		}
	}
}
