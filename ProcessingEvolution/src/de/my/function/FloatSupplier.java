/**
 * 
 */
package de.my.function;

/**
 * Represents a supplier of float-valued results. This is the float-producing
 * primitive specialization of Supplier.
 * 
 * There is no requirement that a distinct result be returned each time the
 * supplier is invoked.
 * 
 * This is a functional interface whose functional method is
 * {@link #getAsFloat()}.
 * 
 * @author Oliver Meyer
 *
 */
@FunctionalInterface
public interface FloatSupplier {
	/**
	 * Gets a float result.
	 * @return a result
	 */
	abstract float getAsFloat();
}
