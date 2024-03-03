package com.avereon.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * A POJO that is a simple representation of a person that is intended to
 * be marshalled and unmarshalled.
 */
@Setter
@Getter
@JsonIgnoreProperties( ignoreUnknown = true )
public class Person implements Serializable {

	private String name;

	private String email;

	private String timezone;

	@Override
	public String toString() {
		return name;
	}

}
