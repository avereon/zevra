package com.avereon.util;

import java.io.Serializable;

/**
 * A POJO that is a simple representation of a person that is intended to
 * be marshalled and unmarshalled.
 */
public class Person implements Serializable {

	private String name;

	private String email;

	private String timezone;

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail( String email ) {
		this.email = email;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone( String timezone ) {
		this.timezone = timezone;
	}

	@Override
	public String toString() {
		return name;
	}

}
