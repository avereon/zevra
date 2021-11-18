package com.avereon.settings;

import com.avereon.event.EventType;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class SettingsEventAssert extends AbstractAssert<SettingsEventAssert, SettingsEvent> {

	public static SettingsEventAssert assertThat( SettingsEvent actual ) {
		return new SettingsEventAssert( actual );
	}

	protected SettingsEventAssert( SettingsEvent actual ) {
		super( actual, SettingsEventAssert.class );
	}

	public SettingsEventAssert hasValues( Settings source, EventType<SettingsEvent> type, String path, String key, Object oldValue, Object newValue ) {
		if( !Objects.equals( actual.getSettings(), source ) ) failWithMessage( "Event settings does not match" );
		if( !Objects.equals( actual.getEventType(), type ) ) failWithMessage( "Event key does not match" );
		if( !Objects.equals( actual.getPath(), path ) ) failWithMessage( "Event path does not match" );
		if( !Objects.equals( actual.getKey(), key ) ) failWithMessage( "Event key does not match" );
		if( !Objects.equals( actual.getOldValue(), oldValue ) ) failWithMessage( "Event oldValue does not match" );
		if( !Objects.equals( actual.getNewValue(), newValue ) ) failWithMessage( "Event newValue does not match" );
		return this;
	}
}
