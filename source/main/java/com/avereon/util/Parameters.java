package com.avereon.util;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * The Parameters class is used to convert command line parameters into an object.
 *
 * <h2>Single Value Parameters</h2>Single value parameters start with &quot;-&quot; and may be followed by an optional value. If the parameter is not followed by a value but instead is followed by another parameter, the value for the
 * parameter is set to &quot;true&quot;. <blockquote> Examples: <table border="1" cellspacing="0"> <tr> <th align="left"><code>-test</code></th> <td>Sets the <code>test</code> parameter to <code>true</code>.</td> </tr> <tr> <th
 * align="left"><code>-level debug</code></th> <td>Sets the <code>level</code> parameter to <code>debug</code>.</td> </tr> <tr> <th align="left"><code>-test -level debug</code></th> <td>Sets the <code>test</code> parameter to
 * <code>true</code> and the <code>level</code> parameter to <code>debug</code>.</td> </tr> </table> </blockquote>
 *
 * <h2>Multiple Value Parameters</h2>Multiple value parameters start with &quot;--&quot; and may be followed by an optional list of values. If the parameter is not followed by a value but instead is followed by another parameter, the value
 * for the parameter is set to &quot;true&quot;. <blockquote> Examples: <table border="1" cellspacing="0"> <tr> <th align="left"><code>--levels</code></th> <td>Sets the <code>levels</code> parameter to <code>true</code>.</td> </tr> <tr> <th
 * align="left"><code>--levels debug info warn</code></th> <td>Sets the <code>levels</code> parameter to the list <code>[debug, info, warn]</code>.</td> </tr> </table> </blockquote> <h2>Files</h2> File names are specified after all
 * parameters have been specified. A file name is a non-parameter value or value after the terminator &quot;--&quot; string. Be aware that once the Parameters class thinks that it has found a file name, all parameters after that are
 * considered file names. <blockquote> Examples: <table border="1" cellspacing="0"> <tr> <th align="left"><code>apple.txt</code></th> <td>Adds the file <code>apple.txt</code> to the list of files.</td> </tr> <tr> <th
 * align="left"><code>-test -- apple.txt</code></th> <td>Sets the <code>test</code> parameter to <code>true</code> and adds the file <code>apple.txt</code> to the list of files.</td> </tr> <tr> <th align="left"><code>-level debug
 * apple.txt</code></th> <td>Sets the <code>level</code> parameter to <code>debug</code> and adds the file <code>apple.txt</code> to the list of files.</td> </tr> <tr> <th align="left"><code>apple.txt -level debug</code></th> <td>Add the
 * files <code>apple.txt</code>, <code>-level</code>, and <code>debug</code> to the list of files.</td> </tr> </table> </blockquote>
 *
 * <h2>Validation</h2> The Parameters class can validate command line parameters by passing a set of valid parameters to the parse() method. If a parameter is specified in the command line that does not match the valid set an
 * InvalidParameterException is thrown.
 * <p>
 *
 * @author Mark Soderquist
 */
public class Parameters {

	private static final String SINGLE = "-";

	private static final String DOUBLE = "--";

	private static final String EQUALS = "=";

	private List<String> originalCommands;

	private List<String> resolvedCommands;

	private Set<String> flags;

	private Map<String, List<String>> values;

	private List<String> uris;

	private Parameters( List<String> originalCommands, List<String> resolvedCommands, Set<String> flags, Map<String, List<String>> values, List<String> uris ) {
		this.originalCommands = new ArrayList<>( originalCommands );
		this.resolvedCommands = new ArrayList<>( resolvedCommands );
		this.flags = flags;
		this.values = values;
		this.uris = uris;
	}

	public static Parameters create() {
		return parse();
	}

	public static Parameters parse( String... commands ) {
		return parse( commands, (Set<String>)null );
	}

	public static Parameters parse( List<String> commands ) {
		return parse( commands.toArray( new String[ 0 ] ) );
	}

	public static Parameters parse( String[] commands, String... validCommands ) {
		return parse( commands, new HashSet<>( Arrays.asList( validCommands ) ) );
	}

	public static Parameters parse( List<String> commands, String... validCommands ) {
		return parse( commands.toArray( new String[ 0 ] ), validCommands );
	}

	public static Parameters parse( String[] commands, Set<String> validCommands ) {
		return parse( Arrays.asList( commands ), validCommands );
	}

	public static Parameters parse( List<String> commands, Set<String> validCommands ) {
		Set<String> flags = new HashSet<>();
		Map<String, List<String>> values = new HashMap<>();
		List<String> uris = new ArrayList<>();

		List<String> resolved = new ArrayList<>( commands );

		boolean terminated = false;

		for( int index = 0; index < commands.size(); index++ ) {
			String command = commands.get( index );

			if( command == null ) throw new IllegalArgumentException( "Null command at index: " + index );

			if( DOUBLE.equals( command ) ) {
				terminated = true;
			} else if( !terminated && command.startsWith( DOUBLE ) ) {
				if( validCommands != null && !validCommands.contains( command ) ) throw new InvalidParameterException( "Unknown parameter: " + command );

				List<String> valueList;
				if( command.contains( EQUALS ) ) {
					String[] parts = command.split( "=" );
					command = parts[ 0 ];
					String value = parts[ 1 ];

					valueList = values.get( removePrefix( command ) );
					if( valueList == null ) valueList = new ArrayList<>();
					valueList.add( value );
				} else {
					valueList = values.get( removePrefix( command ) );
					if( valueList == null ) valueList = new ArrayList<>();
					while( (commands.size() > index + 1) && (!commands.get( index + 1 ).startsWith( SINGLE )) ) {
						String value = commands.get( index + 1 );
						if( value.startsWith( "\\-" ) ) value = value.substring( 1 );
						valueList.add( value );
						index++;
					}
					if( valueList.size() == 0 ) valueList.add( "true" );
				}

				flags.add( command );
				values.put( removePrefix( command ), valueList );
			} else if( !terminated && command.startsWith( SINGLE ) ) {
				if( validCommands != null && !validCommands.contains( command ) ) throw new InvalidParameterException( "Unknown command: " + command );

				List<String> valueList = values.get( removePrefix( command ) );
				if( valueList == null ) valueList = new ArrayList<>();
				if( (commands.size() > index + 1) && (!commands.get( index + 1 ).startsWith( SINGLE )) ) {
					valueList.add( commands.get( index + 1 ) );
					index++;
				}
				if( valueList.size() == 0 ) valueList.add( "true" );

				flags.add( command );
				values.put( removePrefix( command ), valueList );
			} else {
				terminated = true;
				resolved.set( index, UriUtil.resolve( command ).toString() );
				uris.add( resolved.get( index ) );
			}
		}

		return new Parameters( commands, resolved, flags, values, uris );
	}

	public int size() {
		return values.size();
	}

	public Parameters add( Parameters parameters ) {
		if( parameters == null ) return this;

		flags.addAll( parameters.flags );

		for( String flag : parameters.getFlags() ) {
			List<String> oldValues = getValues( flag );
			List<String> newValues = parameters.getValues( flag );
			if( oldValues == null ) {
				values.put( removePrefix( flag ), newValues );
			} else {
				if( !isTrue( flag ) ) oldValues.addAll( newValues );
			}
		}

		for( String uri : parameters.getUris() ) {
			if( !uris.contains( uri ) ) uris.add( uri );
		}

		return this;
	}

	public String get( String flag ) {
		List<String> values = this.values.get( removePrefix( flag ) );
		return values == null ? null : values.get( 0 );
	}

	public String get( String flag, String defaultValue ) {
		String value = get( flag );
		return value != null ? value : defaultValue;
	}

	/**
	 * Returns the parameter value as a boolean. The boolean returned represents the value true if the parameter value is not null and is equal, ignoring case, to the string "true".
	 *
	 * @param flag
	 * @return
	 */
	public boolean isTrue( String flag ) {
		return Boolean.parseBoolean( get( flag ) );
	}

	/**
	 * Returns the parameter value as a boolean if it was defined on the command line. The boolean returned represents the value true if the parameter value is not null and is equal, ignoring case, to the string "true". If the parameter was
	 * not specified on the command line then the default value is returned.
	 *
	 * @param flag
	 * @param defaultValue
	 * @return
	 */
	public boolean isSet( String flag, boolean defaultValue ) {
		String value = get( flag );
		return value == null ? defaultValue : Boolean.parseBoolean( value );
	}

	/**
	 * Returns whether the parameter was specified on the command line.
	 *
	 * @param flag
	 * @return
	 */
	public boolean isSet( String flag ) {
		return !(get( flag ) == null);
	}

	public Set<String> getFlags() {
		return flags;
	}

	public List<String> getValues( String flag ) {
		return values.get( removePrefix( flag ) );
	}

	public List<String> getUris() {
		return uris;
	}

	public List<String> getOriginalCommands() {
		return originalCommands;
	}

	public List<String> getResolvedCommands() {
		return resolvedCommands;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for( String command : originalCommands ) {
			builder.append( command );
			builder.append( " " );
		}

		return builder.toString().trim();
	}

	@Override
	public int hashCode() {
		int code = 0;

		for( String command : resolvedCommands ) {
			code &= command.hashCode();
		}

		return code;
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof Parameters) ) return false;

		Parameters that = (Parameters)object;

		if( this.resolvedCommands.size() != that.resolvedCommands.size() ) return false;

		int count = this.resolvedCommands.size();
		for( int index = 0; index < count; index++ ) {
			if( !TextUtil.areEqual( this.resolvedCommands.get( index ), that.resolvedCommands.get( index ) ) ) return false;
		}

		return true;
	}

	private static String removePrefix( String flag ) {
		if( flag.startsWith( DOUBLE ) ) {
			return flag.substring( DOUBLE.length() );
		} else if( flag.startsWith( SINGLE ) ) {
			return flag.substring( SINGLE.length() );
		}
		return flag;
	}

}
