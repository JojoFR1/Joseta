package dev.jojofr.joseta.utils.function;

/**
 * A functional interface representing a lambda function that takes parameters of type P1 and P2, and returns a result of type R.
 *
 * @param <R> the return type
 * @param <P1> the first parameter type
 * @param <P2> the second parameter type
 */
public interface Function2<R, P1, P2> {
    R get(P1 param1, P2 param2);
}
