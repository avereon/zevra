package com.avereon.result;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A container object which may or may not contain a non-{@code null} value.
 * If a value is present, {@code isPresent()} returns {@code true}. If no
 * value is present, the object is considered <i>empty</i> and
 * {@code isPresent()} returns {@code false}.
 *
 * <p>Additional methods that depend on the presence or absence of a contained
 * value are provided, such as {@link #orElse(Object) orElse()}
 * (returns a default value if no value is present) and
 * {@link #ifPresent(Consumer) ifPresent()} (performs an
 * action if a value is present).
 *
 * <p>This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class. As such, programmers should treat instances that are
 * {@linkplain #equals(Object) equal} as interchangeable and should not
 * use instances for synchronization.
 *
 * @param <T> the type of the value
 * @since 0.8
 */
public class Result<T> {

	private static final Result<?> EMPTY = new Result<>( null );

	private final T value;

	private final Exception exception;

	private Result( T value ) {
		this( value, null );
	}

	private Result( T value, Exception exception ) {
		this.value = value;
		this.exception = exception;
	}

	@SuppressWarnings( "unchecked" )
	public static <T> Result<T> of( T value ) {
		return value == null ? (Result<T>)EMPTY : new Result<>( value );
	}

	@SuppressWarnings( "unchecked" )
	public static <T> Result<T> of( Exception exception ) {
		return exception == null ? (Result<T>)EMPTY : new Result<>( null, exception );
	}

	@SuppressWarnings( "unchecked" )
	public static <T> Result<T> empty() {
		return (Result<T>)EMPTY;
	}

	/**
	 * If a value is present, returns the value, otherwise throws
	 * {@code NoSuchElementException}.
	 *
	 * @return the non-{@code null} value described by this {@code Result}
	 * @throws NoSuchElementException if no value is present
	 * @apiNote The preferred alternative to this method is {@link #orElseThrow()}.
	 */
	public T get() {
		if( value == null ) throw new NoSuchElementException( "No value present" );
		return value;
	}

	/**
	 * If a value is present, returns {@code true}, otherwise {@code false}.
	 *
	 * @return {@code true} if a value is present, otherwise {@code false}
	 */
	public boolean isPresent() {
		return value != null;
	}

	/**
	 * If a value is not present, returns {@code true}, otherwise {@code false}.
	 *
	 * @return {@code true} if a value is not present, otherwise {@code false}
	 */
	public boolean isEmpty() {
		return value == null;
	}

	public boolean isSuccessful() {
		return exception == null;
	}

	public boolean isFailed() {
		return exception != null;
	}

	/**
	 * If a value is present, performs the given action with the value,
	 * otherwise does nothing.
	 *
	 * @param action the action to be performed, if a value is present
	 * @throws NullPointerException if value is present and the given action is
	 * {@code null}
	 */
	public void ifPresent( Consumer<? super T> action ) {
		if( isPresent() ) action.accept( value );
	}

	public void ifSuccess(Consumer<? super T> action ) {
		if( isPresent() && isSuccessful() ) action.accept( value );
	}

	public void ifFailure(Consumer<Exception> action ) {
		if( isFailed() ) action.accept( exception );
	}

	/**
	 * If a value is present, performs the given action with the value,
	 * otherwise performs the given empty-based action.
	 *
	 * @param action the action to be performed, if a value is present
	 * @param emptyAction the empty-based action to be performed, if no value is present
	 * @throws NullPointerException if a value is present and the given action
	 * is {@code null}, or no value is present and the given empty-based
	 * action is {@code null}.
	 */
	public void ifPresentOrElse( Consumer<? super T> action, Runnable emptyAction ) {
		if( value != null ) {
			action.accept( value );
		} else {
			emptyAction.run();
		}
	}

	/**
	 * If a value is present, and the value matches the given predicate,
	 * returns an {@code Result} describing the value, otherwise returns an
	 * empty {@code Result}.
	 *
	 * @param predicate the predicate to apply to a value, if present
	 * @return an {@code Result} describing the value of this
	 * {@code Result}, if a value is present and the value matches the
	 * given predicate, otherwise an empty {@code Result}
	 * @throws NullPointerException if the predicate is {@code null}
	 */
	public Result<T> filter( Predicate<? super T> predicate ) {
		Objects.requireNonNull( predicate );
		if( !isPresent() ) {
			return this;
		} else {
			return predicate.test( value ) ? this : empty();
		}
	}

	/**
	 * If a value is present, returns an {@code Result} describing (as if by
	 * {@link #of}) the result of applying the given mapping function to
	 * the value, otherwise returns an empty {@code Result}.
	 *
	 * <p>If the mapping function returns a {@code null} result then this method
	 * returns an empty {@code Result}.
	 *
	 * @param mapper the mapping function to apply to a value, if present
	 * @param <U> The type of the value returned from the mapping function
	 * @return an {@code Result} describing the result of applying a mapping
	 * function to the value of this {@code Result}, if a value is
	 * present, otherwise an empty {@code Result}
	 * @throws NullPointerException if the mapping function is {@code null}
	 * @apiNote This method supports post-processing on {@code Result} values, without
	 * the need to explicitly check for a return status.  For example, the
	 * following code traverses a stream of URIs, selects one that has not
	 * yet been processed, and creates a path from that URI, returning
	 * an {@code Result<Path>}:
	 *
	 * <pre>{@code
	 *     Result<Path> p =
	 *         uris.stream().filter(uri -> !isProcessedYet(uri))
	 *                       .findFirst()
	 *                       .map(Paths::get);
	 * }</pre>
	 * <p>
	 * Here, {@code findFirst} returns an {@code Result<URI>}, and then
	 * {@code map} returns an {@code Result<Path>} for the desired
	 * URI if one exists.
	 */
	public <U> Result<U> map( Function<? super T, ? extends U> mapper ) {
		Objects.requireNonNull( mapper );
		if( !isPresent() ) {
			return empty();
		} else {
			return Result.of( mapper.apply( value ) );
		}
	}

	/**
	 * If a value is present, returns the result of applying the given
	 * {@code Result}-bearing mapping function to the value, otherwise returns
	 * an empty {@code Result}.
	 *
	 * <p>This method is similar to {@link #map(Function)}, but the mapping
	 * function is one whose result is already an {@code Result}, and if
	 * invoked, {@code flatMap} does not wrap it within an additional
	 * {@code Result}.
	 *
	 * @param <U> The type of value of the {@code Result} returned by the
	 * mapping function
	 * @param mapper the mapping function to apply to a value, if present
	 * @return the result of applying an {@code Result}-bearing mapping
	 * function to the value of this {@code Result}, if a value is
	 * present, otherwise an empty {@code Result}
	 * @throws NullPointerException if the mapping function is {@code null} or
	 * returns a {@code null} result
	 */
	public <U> Result<U> flatMap( Function<? super T, ? extends Result<? extends U>> mapper ) {
		Objects.requireNonNull( mapper );
		if( !isPresent() ) {
			return empty();
		} else {
			@SuppressWarnings( "unchecked" ) Result<U> r = (Result<U>)mapper.apply( value );
			return Objects.requireNonNull( r );
		}
	}

	/**
	 * If a value is present, returns an {@code Result} describing the value,
	 * otherwise returns an {@code Result} produced by the supplying function.
	 *
	 * @param supplier the supplying function that produces an {@code Result}
	 * to be returned
	 * @return returns an {@code Result} describing the value of this
	 * {@code Result}, if a value is present, otherwise an
	 * {@code Result} produced by the supplying function.
	 * @throws NullPointerException if the supplying function is {@code null} or
	 * produces a {@code null} result
	 * @since 9
	 */
	public Result<T> or( Supplier<? extends Result<? extends T>> supplier ) {
		Objects.requireNonNull( supplier );
		if( isPresent() ) {
			return this;
		} else {
			@SuppressWarnings( "unchecked" ) Result<T> r = (Result<T>)supplier.get();
			return Objects.requireNonNull( r );
		}
	}

	/**
	 * If a value is present, returns a sequential {@link Stream} containing
	 * only that value, otherwise returns an empty {@code Stream}.
	 *
	 * @return the result value as a {@code Stream}
	 * @apiNote This method can be used to transform a {@code Stream} of result
	 * elements to a {@code Stream} of present value elements:
	 * <pre>{@code
	 *     Stream<Result<T>> os = ..
	 *     Stream<T> s = os.flatMap(Result::stream)
	 * }</pre>
	 */
	public Stream<T> stream() {
		if( !isPresent() ) {
			return Stream.empty();
		} else {
			return Stream.of( value );
		}
	}

	/**
	 * If a value is present, returns the value, otherwise returns {@code other}.
	 *
	 * @param other the value to be returned, if no value is present.
	 * May be {@code null}.
	 * @return the value, if present, otherwise {@code other}
	 */
	public T orElse( T other ) {
		return value != null ? value : other;
	}

	/**
	 * If a value is present, returns the value, otherwise returns the result
	 * produced by the supplying function.
	 *
	 * @param supplier the supplying function that produces a value to be returned
	 * @return the value, if present, otherwise the result produced by the
	 * supplying function
	 * @throws NullPointerException if no value is present and the supplying
	 * function is {@code null}
	 */
	public T orElseGet( Supplier<? extends T> supplier ) {
		return value != null ? value : supplier.get();
	}

	/**
	 * If a value is present, returns the value, otherwise throws
	 * {@code NoSuchElementException}.
	 *
	 * @return the non-{@code null} value described by this {@code Result}
	 * @throws NoSuchElementException if no value is present
	 * @since 10
	 */
	public T orElseThrow() {
		if( value == null ) throw new NoSuchElementException( "No value present" );
		return value;
	}

	/**
	 * If a value is present, returns the value, otherwise throws an exception
	 * produced by the exception supplying function.
	 *
	 * @param <X> Type of the exception to be thrown
	 * @param exceptionSupplier the supplying function that produces an
	 * exception to be thrown
	 * @return the value, if present
	 * @throws X if no value is present
	 * @throws NullPointerException if no value is present and the exception
	 * supplying function is {@code null}
	 * @apiNote A method reference to the exception constructor with an empty argument
	 * list can be used as the supplier. For example,
	 * {@code IllegalStateException::new}
	 */
	public <X extends Throwable> T orElseThrow( Supplier<? extends X> exceptionSupplier ) throws X {
		if( value != null ) {
			return value;
		} else {
			throw exceptionSupplier.get();
		}
	}

	@SuppressWarnings( "unchecked" )
	public <S> Result<S> andThen(Function<Result<T>, Result<S>> function ) {
		if( function != null ) return function.apply( this );
		return (Result<S>)EMPTY;
	}

	public Exception getException() {
		return exception;
	}

	public void tryException() throws Exception {
		if( exception == null ) return;
		throw exception;
	}

	/**
	 * Indicates whether some other object is "equal to" this {@code Result}.
	 * The other object is considered equal if:
	 * <ul>
	 * <li>it is also an {@code Result} and;
	 * <li>both instances have no value present or;
	 * <li>the present values are "equal to" each other via {@code equals()}.
	 * </ul>
	 *
	 * @param object an object to be tested for equality
	 * @return {@code true} if the other object is "equal to" this object
	 * otherwise {@code false}
	 */
	@Override
	public boolean equals( Object object ) {
		if( this == object ) return true;
		return object instanceof Result<?> && Objects.equals( value, ((Result<?>)object).value );
	}

	/**
	 * Returns the hash code of the value, if present, otherwise {@code 0}
	 * (zero) if no value is present.
	 *
	 * @return hash code value of the present value or {@code 0} if no value is
	 * present
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode( value );
	}

	/**
	 * Returns a non-empty string representation of this {@code Result}
	 * suitable for debugging.  The exact presentation format is unspecified and
	 * may vary between implementations and versions.
	 *
	 * @return the string representation of this instance
	 * @implSpec If a value is present the result must include its string representation
	 * in the result.  Empty and present {@code Result}s must be unambiguously
	 * differentiable.
	 */
	@Override
	public String toString() {
		return value != null ? String.format( "Result[%s]", value ) : "Result.EMPTY";
	}

}
