package com.avereon.skill;

public interface Identified {

	// NOTE Do not change the value of this key without a plan to migrate persisted values
	String KEY = "id";

	String getProductId();

	void setProductId( String id );

}
