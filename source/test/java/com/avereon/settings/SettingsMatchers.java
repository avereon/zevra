package com.avereon.settings;

import com.avereon.event.EventType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public class SettingsMatchers {

	public static Matcher<SettingsEvent> eventHas( Settings source, EventType<SettingsEvent> type, String path, String key, Object oldValue, Object newValue ) {
		return new TypeSafeMatcher<>() {

			@Override
			public void describeTo( final Description description ) {
				description.appendValue( new SettingsEvent( source, type, path, key, oldValue, newValue ) );
			}

			@Override
			protected void describeMismatchSafely( final SettingsEvent event, final Description mismatchDescription ) {
				mismatchDescription.appendValue( event );
			}

			@Override
			protected boolean matchesSafely( final SettingsEvent event ) {
				return Objects.equals( event.getSource(), source ) && Objects.equals( event.getKey(), key ) && Objects.equals(
					event.getEventType(),
					type
				) && Objects.equals( event.getPath(), path ) && Objects.equals( event.getOldValue(), oldValue ) && Objects.equals( event.getNewValue(), newValue );
			}
		};
	}

}
