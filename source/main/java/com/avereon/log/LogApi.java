package com.avereon.log;

/**
 * The LogApi interface is patterned after the very nice Fluent Logger from
 * Google. The only reason the Fluent Logger was not used directly was because
 * the libraries were not Java module compatible and therefore caused problems
 * when loading.
 *
 * @param <API>
 */
public interface LogApi<API extends LogApi<API>> {

	API withCause( Throwable cause );

	//API every(int n);

	//API atMostEvery(int n, TimeUnit unit);

	//API withStackTrace( StackSize size);

	<T> API with( Object key, T value );

	//API withInjectedLogSite(LogSite logSite);

	//API withInjectedLogSite( String internalClassName, String methodName, int encodedLineNumber, String sourceFileName);

	boolean isEnabled();

	boolean isLiteral();

	void log();

	void log( String message );

	void log( String message, Object p1 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3, Object p4 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object... rest );

	// ---- Overloads for a single argument (to avoid auto-boxing and vararg array creation). ----

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1 );

	// ---- Overloads for two arguments (to avoid auto-boxing and vararg array creation). ----
	/*
	 * It may not be obvious why we need _all_ combinations of fundamental types here (because some
	 * combinations should be rare enough that we can ignore them). However due to the precedence in
	 * the Java compiler for converting fundamental types in preference to auto-boxing, and the need
	 * to preserve information about the original type (byte, short, char etc...) when doing unsigned
	 * formatting, it turns out that all combinations are required.
	 */

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, Object p1, double p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, Object p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, boolean p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, char p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, byte p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, short p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, int p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, long p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, float p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, boolean p1, double p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, char p1, double p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, byte p1, double p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, short p1, double p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, int p1, double p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, long p1, double p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, float p1, double p2 );

	/** Logs a message with formatted arguments (see {@link #log(String, Object)} for details). */
	void log( String message, double p1, double p2 );

	void logVarargs( String message, Object... args );

	public static class NoOp<API extends LogApi<API>> implements LogApi<API> {

		@SuppressWarnings( "unchecked" )
		protected final API noOp() {
			return (API)this;
		}

		@Override
		public final boolean isEnabled() {
			return false;
		}

		@Override
		public boolean isLiteral() {
			return true;
		}

		@Override
		public final <T> API with( Object key, T value ) {
			return noOp();
		}

		@Override
		public final API withCause( Throwable cause ) {
			return noOp();
		}

		//		@Override
		//		public API withStackTrace( StackSize size ) {
		//			// Don't permit null since NONE is the right thing to use.
		//			checkNotNull( size, "stack size" );
		//			return noOp();
		//		}

				@Override
				public final void logVarargs( String message, Object[] params ) {}

		@Override
		public final void log() {}

		@Override
		public final void log( String message ) {}

		@Override
		public final void log( String message, Object p1 ) {}

		@Override
		public final void log( String message, Object p1, Object p2 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3, Object p4 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10 ) {}

		@Override
		public final void log( String message, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9, Object p10, Object... rest ) {}

		@Override
		public final void log( String message, char p1 ) {}

		@Override
		public final void log( String message, byte p1 ) {}

		@Override
		public final void log( String message, short p1 ) {}

		@Override
		public final void log( String message, int p1 ) {}

		@Override
		public final void log( String message, long p1 ) {}

		@Override
		public final void log( String message, Object p1, boolean p2 ) {}

		@Override
		public final void log( String message, Object p1, char p2 ) {}

		@Override
		public final void log( String message, Object p1, byte p2 ) {}

		@Override
		public final void log( String message, Object p1, short p2 ) {}

		@Override
		public final void log( String message, Object p1, int p2 ) {}

		@Override
		public final void log( String message, Object p1, long p2 ) {}

		@Override
		public final void log( String message, Object p1, float p2 ) {}

		@Override
		public final void log( String message, Object p1, double p2 ) {}

		@Override
		public final void log( String message, boolean p1, Object p2 ) {}

		@Override
		public final void log( String message, char p1, Object p2 ) {}

		@Override
		public final void log( String message, byte p1, Object p2 ) {}

		@Override
		public final void log( String message, short p1, Object p2 ) {}

		@Override
		public final void log( String message, int p1, Object p2 ) {}

		@Override
		public final void log( String message, long p1, Object p2 ) {}

		@Override
		public final void log( String message, float p1, Object p2 ) {}

		@Override
		public final void log( String message, double p1, Object p2 ) {}

		@Override
		public final void log( String message, boolean p1, boolean p2 ) {}

		@Override
		public final void log( String message, char p1, boolean p2 ) {}

		@Override
		public final void log( String message, byte p1, boolean p2 ) {}

		@Override
		public final void log( String message, short p1, boolean p2 ) {}

		@Override
		public final void log( String message, int p1, boolean p2 ) {}

		@Override
		public final void log( String message, long p1, boolean p2 ) {}

		@Override
		public final void log( String message, float p1, boolean p2 ) {}

		@Override
		public final void log( String message, double p1, boolean p2 ) {}

		@Override
		public final void log( String message, boolean p1, char p2 ) {}

		@Override
		public final void log( String message, char p1, char p2 ) {}

		@Override
		public final void log( String message, byte p1, char p2 ) {}

		@Override
		public final void log( String message, short p1, char p2 ) {}

		@Override
		public final void log( String message, int p1, char p2 ) {}

		@Override
		public final void log( String message, long p1, char p2 ) {}

		@Override
		public final void log( String message, float p1, char p2 ) {}

		@Override
		public final void log( String message, double p1, char p2 ) {}

		@Override
		public final void log( String message, boolean p1, byte p2 ) {}

		@Override
		public final void log( String message, char p1, byte p2 ) {}

		@Override
		public final void log( String message, byte p1, byte p2 ) {}

		@Override
		public final void log( String message, short p1, byte p2 ) {}

		@Override
		public final void log( String message, int p1, byte p2 ) {}

		@Override
		public final void log( String message, long p1, byte p2 ) {}

		@Override
		public final void log( String message, float p1, byte p2 ) {}

		@Override
		public final void log( String message, double p1, byte p2 ) {}

		@Override
		public final void log( String message, boolean p1, short p2 ) {}

		@Override
		public final void log( String message, char p1, short p2 ) {}

		@Override
		public final void log( String message, byte p1, short p2 ) {}

		@Override
		public final void log( String message, short p1, short p2 ) {}

		@Override
		public final void log( String message, int p1, short p2 ) {}

		@Override
		public final void log( String message, long p1, short p2 ) {}

		@Override
		public final void log( String message, float p1, short p2 ) {}

		@Override
		public final void log( String message, double p1, short p2 ) {}

		@Override
		public final void log( String message, boolean p1, int p2 ) {}

		@Override
		public final void log( String message, char p1, int p2 ) {}

		@Override
		public final void log( String message, byte p1, int p2 ) {}

		@Override
		public final void log( String message, short p1, int p2 ) {}

		@Override
		public final void log( String message, int p1, int p2 ) {}

		@Override
		public final void log( String message, long p1, int p2 ) {}

		@Override
		public final void log( String message, float p1, int p2 ) {}

		@Override
		public final void log( String message, double p1, int p2 ) {}

		@Override
		public final void log( String message, boolean p1, long p2 ) {}

		@Override
		public final void log( String message, char p1, long p2 ) {}

		@Override
		public final void log( String message, byte p1, long p2 ) {}

		@Override
		public final void log( String message, short p1, long p2 ) {}

		@Override
		public final void log( String message, int p1, long p2 ) {}

		@Override
		public final void log( String message, long p1, long p2 ) {}

		@Override
		public final void log( String message, float p1, long p2 ) {}

		@Override
		public final void log( String message, double p1, long p2 ) {}

		@Override
		public final void log( String message, boolean p1, float p2 ) {}

		@Override
		public final void log( String message, char p1, float p2 ) {}

		@Override
		public final void log( String message, byte p1, float p2 ) {}

		@Override
		public final void log( String message, short p1, float p2 ) {}

		@Override
		public final void log( String message, int p1, float p2 ) {}

		@Override
		public final void log( String message, long p1, float p2 ) {}

		@Override
		public final void log( String message, float p1, float p2 ) {}

		@Override
		public final void log( String message, double p1, float p2 ) {}

		@Override
		public final void log( String message, boolean p1, double p2 ) {}

		@Override
		public final void log( String message, char p1, double p2 ) {}

		@Override
		public final void log( String message, byte p1, double p2 ) {}

		@Override
		public final void log( String message, short p1, double p2 ) {}

		@Override
		public final void log( String message, int p1, double p2 ) {}

		@Override
		public final void log( String message, long p1, double p2 ) {}

		@Override
		public final void log( String message, float p1, double p2 ) {}

		@Override
		public final void log( String message, double p1, double p2 ) {}

	}

}
