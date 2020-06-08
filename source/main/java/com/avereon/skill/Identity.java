package com.avereon.skill;

public interface Identity {

	// NOTE Do not change the value of this key without a plan to migrate persisted values
	String KEY = "id";

	String getUid();

}
