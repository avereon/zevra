package com.avereon.skill;

public interface Identity {

	// NOTE Do not change the value of this key without a plan to migrate persisted values
	String KEY = "id";

	// TODO Rename to getTag()
	// TODO Rename to getIdent()
	// TODO Rename to getIdKey()
	String getProductId();

}
