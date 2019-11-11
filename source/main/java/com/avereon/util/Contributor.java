package com.avereon.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A POJO that represents a person that contributes to a cause.
 */
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
