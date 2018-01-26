package de.my.function;

/**
 * Represents an operation upon two float-valued operands and producing a float-valued result. This is the primitive
 * type specialization of BinaryOperator for float.
 * 
 * This is a functional interface whose functional method {@link #applyAsFloat(float, float)}.
 * 
 * @author Oliver Meyer
 *
 */
public interface FloatBinaryOperation {

    /**
     * Combines two arguments and return a float.
     * 
     * @param arg0
     *            the 1st argument.
     * @param arg1
     *            the 2nd argument
     * @return the result
     */
    abstract float applyAsFloat(float arg0, float arg1);
}
