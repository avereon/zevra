package com.avereon.annotation;

import java.lang.annotation.*;

@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.SOURCE )
@Repeatable( Notes.class )
@Documented
@Inherited
public @interface Note {

	String value();

}
