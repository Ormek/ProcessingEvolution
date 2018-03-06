package de.my;


/**
 * This Giving Order does not deny any presents to anyone.
 * @author Oliver Meyer
 *
 */
public class Complete extends GivingOrder {
	
	/** 
	 * {@inheritDoc}
	 */
	public boolean[][] init() {
		boolean value;
		boolean[][] init = new boolean[PERSON_COUNT][PERSON_COUNT];
		for (Person from : Person.values()) {
			for (Person to : Person.values()) {
				if (from == to) {
					value = false;
				} else {
					value = true;
				}
				init[from.ordinal()][to.ordinal()] = value;
			}
		}
		return init;
	}

}
