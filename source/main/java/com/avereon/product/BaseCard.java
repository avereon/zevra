package com.avereon.product;

import com.avereon.util.IdGenerator;

public abstract class BaseCard {

	private String internalId;

	protected BaseCard() {
		this.internalId = IdGenerator.getId();
	}

	public String getInternalId() {
		return internalId;
	}

	@SuppressWarnings( "UnusedReturnValue" )
	public BaseCard setInternalId( String internalId ) {
		this.internalId = internalId;
		return this;
	}

}
