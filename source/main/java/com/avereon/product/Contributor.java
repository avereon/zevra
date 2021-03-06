package com.avereon.product;

import com.avereon.util.Person;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A POJO that represents a person that contributes to a cause.
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class Contributor extends Person implements Serializable {

	private String organization;

	private String organizationUrl;

	private List<String> roles = new ArrayList<>();

	public String getOrganization() {
		return organization;
	}

	public void setOrganization( String organization ) {
		this.organization = organization;
	}

	public String getOrganizationUrl() {
		return organizationUrl;
	}

	public void setOrganizationUrl( String organizationUrl ) {
		this.organizationUrl = organizationUrl;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles( List<String> roles ) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		return "Contributor{" + "name='" + getName() + " roles=" + roles + '}';
	}

}
