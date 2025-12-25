package joseta.utils.function;

/**
 * A functional interface representing a lambda function that takes a parameter of type P and returns a result of type R.
 *
 * @param <R> the return type
 * @param <P> the parameter type
 */
public interface Function<R, P> {
    /**
     * Applies this function to the given parameter.
     *
     * @param param the function parameter
     * @return the function result
     */
    R get(P param);
}
