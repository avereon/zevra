package com.avereon.annotation;

import java.lang.annotation.*;

@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.CONSTRUCTOR } )
@Retention( RetentionPolicy.SOURCE )
@Repeatable( Notes.class )
@Documented
@Inherited
public @interface Note {

	String value();

}
