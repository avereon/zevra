package com.xeomar.settings;

import java.util.Objects;

public class MockBean {

	private String stringProperty;

	private int integerPrimitiveProperty;

	private Integer integerProperty;

	public String getStringProperty() {
		return stringProperty;
	}

	public void setStringProperty( String stringProperty ) {
		this.stringProperty = stringProperty;
	}

	public int getIntegerPrimitiveProperty() {
		return integerPrimitiveProperty;
	}

	public void setIntegerPrimitiveProperty( int integerPrimitiveProperty ) {
		this.integerPrimitiveProperty = integerPrimitiveProperty;
	}

	public Integer getIntegerProperty() {
		return integerProperty;
	}

	public void setIntegerProperty( Integer integerProperty ) {
		this.integerProperty = integerProperty;
	}

	@Override
	public boolean equals( Object that ) {
		if( this == that ) return true;
		if( !(that instanceof MockBean) ) return false;
		MockBean mockBean = (MockBean)that;
		return integerPrimitiveProperty == mockBean.integerPrimitiveProperty && Objects.equals( stringProperty, mockBean.stringProperty ) && Objects.equals( integerProperty, mockBean.integerProperty );
	}

	@Override
	public int hashCode() {
		return Objects.hash( stringProperty, integerPrimitiveProperty, integerProperty );
	}

}
