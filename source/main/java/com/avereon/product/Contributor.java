package com.avereon.product;

import com.avereon.util.Person;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A POJO that represents a person that contributes to a cause.
 */
@Setter
@Getter
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class Contributor extends Person implements Serializable {

	private String organization;

	private String organizationUrl;

	private List<String> roles = new ArrayList<>();

	@Override
	public String toString() {
		return "Contributor{" + "name='" + getName() + " roles=" + roles + '}';
	}

}
