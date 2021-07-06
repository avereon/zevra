package com.avereon.log;

import com.avereon.log.provider.LoggingProvider;
import com.avereon.util.JavaUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public abstract class LogContext<LOGGER extends AbstractLogger<API>, API extends LogApi<API>> implements LogApi<API>, LogData {

	private final Level level;

	private final long timestampNanos;

	private String message;

	private Object[] messageParameters;

	private Map<Object, Object> metadata;

	/**
	 * Creates a logging context with the specified level, and with a timestamp
	 * obtained from the configured logging {@link LoggingProvider}.
	 *
	 * @param level the log level for this log statement.
	 * @param isForced whether to force this log statement (see {@link #wasForced()} for details).
	 */
	protected LogContext( Level level, boolean isForced ) {
		this( level, isForced, LoggingProvider.getCurrentTimeNanos() );
	}

	/**
	 * Creates a logging context with the specified level and timestamp. This constructor is provided
	 * only for testing when timestamps need to be injected. In general, subclasses would only need
	 * to call this constructor when testing additional API methods which require timestamps (e.g.
	 * additional rate limiting functionality). Most unit tests for logger subclasses should not
	 * test the value of the timestamp at all, since this is already well tested elsewhere.
	 *
	 * @param level the log level for this log statement.
	 * @param isForced whether to force this log statement (see {@link #wasForced()} for details).
	 * @param timestampNanos the nanosecond timestamp for this log statement.
	 */
	protected LogContext( Level level, boolean isForced, long timestampNanos ) {
		this.level = Objects.requireNonNull( level );
		this.timestampNanos = timestampNanos;
		if( isForced ) addMetadata( LogData.FORCED, Boolean.TRUE );
	}

	/**
	 * Returns the current API (which is just the concrete sub-type of this instance). This is
	 * returned by fluent methods to continue the fluent call chain.
	 */
	protected abstract API api();

	// ---- Logging Context Constants ----

	/**
	 * Returns the logger which created this context. This is implemented as an abstract method to
	 * save a field in every context.
	 */
	protected abstract LOGGER getLogger();

	/**
	 * Returns the constant no-op logging API, which can be returned by fluent methods in extended
	 * logging contexts to efficiently disable logging. This is implemented as an abstract method to
	 * save a field in every context.
	 */
	protected abstract API noOp();

	// LogData -------------------------------------------------------------------

	@Override
	public final Level getLevel() {
		return level;
	}

	@Override
	public final long getTimestampNanos() {
		return timestampNanos;
	}

	@Override
	public final String getLoggerName() {
		return getLogger().getProvider().getLoggerName();
	}

	@Override
	public final boolean wasForced() {
		return metadata != null && Boolean.TRUE.equals( metadata.get( LogData.FORCED ) );
	}

	public String getMessage() {
		return message;
	}

	public Object[] getMessageParameters() {
		return messageParameters == null ? new Object[ 0 ] : messageParameters;
	}

	public Map<Object, Object> getMetadata() {
		return metadata == null ? Map.of() : metadata;
	}

	// LogApi --------------------------------------------------------------------

	@Override
	public final boolean isEnabled() {
		return wasForced() || getLogger().isLoggable( level );
	}

	public final boolean isLiteral() {
		return messageParameters == null || messageParameters.length == 0;
	}

	@Override
	public final <T> API with( Object key, T value ) {
		addMetadata( Objects.requireNonNull( key ), Objects.requireNonNull( value ) );
		return api();
	}

	@Override
	public final API withCause( Throwable cause ) {
		if( cause != null ) addMetadata( LogData.CAUSE, cause );
		return api();
	}

	/*
	 * Note that while all log statements look almost identical to each other, it is vital that we
	 * keep the 'shouldLog()' call outside of the call to 'logImpl()' so we can decide whether or not
	 * to abort logging before we do any varargs creation.
	 */

	@Override
	public final void log() {
		if( shouldLog() ) doLog( "" );
	}

	@Override
	public final void log( String msg ) {
		if( shouldLog() ) doLog( msg );
	}

	@Override
	public final void log( String message, Object p1 ) {
		if( shouldLog() ) doLog( message, p1 );
	}

	@Override
	public final void log( String message, Object p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log(
		String message, Object p1, Object p2, Object p3
	) {
		if( shouldLog() ) doLog( message, p1, p2, p3 );
	}

	@Override
	public final void log(
		String message, Object p1, Object p2, Object p3, Object p4
	) {
		if( shouldLog() ) doLog( message, p1, p2, p3, p4 );
	}

	@Override
	public final void log(
		String msg, Object p1, Object p2, Object p3, Object p4, Object p5
	) {
		if( shouldLog() ) doLog( msg, p1, p2, p3, p4, p5 );
	}

	@Override
	public final void log(
		String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6
	) {
		if( shouldLog() ) doLog( msg, p1, p2, p3, p4, p5, p6 );
	}

	@Override
	public final void log(
		String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7
	) {
		if( shouldLog() ) doLog( msg, p1, p2, p3, p4, p5, p6, p7 );
	}

	@Override
	public final void log(
		String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8
	) {
		if( shouldLog() ) doLog( msg, p1, p2, p3, p4, p5, p6, p7, p8 );
	}

	@Override
	public final void log(
		String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9
	) {
		if( shouldLog() ) doLog( msg, p1, p2, p3, p4, p5, p6, p7, p8, p9 );
	}

	@Override
	public final void log(
		String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10
	) {
		if( shouldLog() ) doLog( msg, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 );
	}

	@Override
	public final void log(
		String msg, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object... rest
	) {
		if( shouldLog() ) {
			// Manually create a new varargs array and copy the parameters in.
			Object[] params = new Object[ rest.length + 10 ];
			params[ 0 ] = p1;
			params[ 1 ] = p2;
			params[ 2 ] = p3;
			params[ 3 ] = p4;
			params[ 4 ] = p5;
			params[ 5 ] = p6;
			params[ 6 ] = p7;
			params[ 7 ] = p8;
			params[ 8 ] = p9;
			params[ 9 ] = p10;
			System.arraycopy( rest, 0, params, 10, rest.length );
			doLog( msg, params );
		}
	}

	@Override
	public final void log( String message, char p1 ) {
		if( shouldLog() ) doLog( message, p1 );
	}

	@Override
	public final void log( String message, byte p1 ) {
		if( shouldLog() ) doLog( message, p1 );
	}

	@Override
	public final void log( String message, short p1 ) {
		if( shouldLog() ) doLog( message, p1 );
	}

	@Override
	public final void log( String message, int p1 ) {
		if( shouldLog() ) doLog( message, p1 );
	}

	@Override
	public final void log( String message, long p1 ) {
		if( shouldLog() ) doLog( message, p1 );
	}

	@Override
	public final void log( String message, Object p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, Object p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, Object p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, Object p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, Object p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, Object p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, Object p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, Object p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, Object p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, boolean p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, char p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, byte p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, short p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, int p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, long p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, float p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, boolean p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, char p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, byte p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, short p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, int p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, long p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, float p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void log( String message, double p1, double p2 ) {
		if( shouldLog() ) doLog( message, p1, p2 );
	}

	@Override
	public final void logVarargs( String message, Object[] params ) {
		if( shouldLog() ) {
			// Copy the varargs array (because we didn't create it and this is quite a rare case).
			doLog( message, Arrays.copyOf( params, params.length ) );
		}
	}

	private void addMetadata( Object key, Object value ) {
		if( metadata == null ) metadata = new ConcurrentHashMap<>();
		metadata.put( key, value );
	}

	private boolean shouldLog() {
		return getLogger().isLoggable( level );
	}

	private void doLog( String message, Object... args ) {
		this.message = message;
		this.messageParameters = args;

		// Evaluate any (rare) LazyEval instances early. This may throw exceptions from user code, but
		// it seems reasonable to propagate them in this case (they would have been thrown if the
		// argument was evaluated at the call site anyway).
		for( int n = 0; n < args.length; n++ ) {
			if( args[ n ] instanceof LazyEval ) {
				args[ n ] = ((LazyEval<?>)args[ n ]).evaluate();
			}
		}

		if( !isLiteral() ) this.message = String.format( message, args );

		try {
			// TODO Add extra metadata
			//addMetadata( MODULE_NAME, JavaUtil.getCallingModuleName() );
			addMetadata( CLASS_NAME, JavaUtil.getCallingClassName() );
			//addMetadata( METHOD_NAME, JavaUtil.getCallingMethodName() );
		} catch( Throwable throwable ) {
			throwable.printStackTrace(System.err);
		}

		getLogger().write( this );
	}

}
