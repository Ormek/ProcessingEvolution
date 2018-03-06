package de.my;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import de.my.GivingOrder.DuplicatePresent;
import de.my.GivingOrder.InvalidPresent;
import de.my.GivingOrder.Person;
import de.my.ROT.InvalidCharSet;
import de.my.GivingOrder;

public class Stammtisch {
	private static int currentIndex;

	private static Set<GivingOrder> allResults = new HashSet<GivingOrder>();

	public static void main(String[] args) {
		try {
			GivingOrder order = new GivingOrder();
			System.out.println(order.printAllowed());
			// Try to iterate all GivingOrders there are

			System.out.println("Index;" + GivingOrder.headCSV());
			// Create an incomplete giving Order

			findOrder(order, 0);

			GivingOrder result = selectOrder(allResults);

			System.out.println(result.printEncrypted());
			
			BirthDates.printAllBirthdays();
		} catch (InvalidCharSet e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Randomly select one of the results.
	 * 
	 * @param results
	 *            the set of results to select one of
	 * @return one order from the set.
	 */
	private static GivingOrder selectOrder(Set<GivingOrder> results) {
		GivingOrder result = null;
		Iterator<GivingOrder> it = results.iterator();
		Random generator = new Random();
		int selector = generator.nextInt(results.size());
		assert selector >= 0 : "nextInt returned negative Number.";
		for (int i = 0; i <= selector; i++) {
			result = it.next();
		}
		if (result == null) {
			throw new RuntimeException(
					"Programming error. Trying to retrieve element " + selector
							+ " from a set with " + results.size()
							+ " elements failed.");
		}
		return result;
	}

	private static void findOrder(GivingOrder order, int nextGiver) {
		try {
			GivingOrder initialState = order.clone();
			for (Person receiver : Person.values()) {
				try {
					order.give(Person.values()[nextGiver], receiver);
					if (order.isComplete()) {
						reportSuccess(order);
					} else {
						findOrder(order, nextGiver + 1);
					}
					order = initialState.clone();
				} catch (DuplicatePresent dp) {
					continue;
				} catch (InvalidPresent ip) {
					continue;
				}
			}
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(
					"Program Error. Please make sure, that Giving Order is cloneable.");
		}

	}

	/**
	 * Prints a complete order and stores a copy in the allResults set.
	 * 
	 * @param order
	 *            to print and store
	 * @throws CloneNotSupportedException
	 *             if the given order cannot be cloned
	 */
	private static void reportSuccess(GivingOrder order)
			throws CloneNotSupportedException {
		currentIndex++;
		allResults.add(order.clone());
		System.out.println(currentIndex + ";" + order.printInCSV());
	}
}
