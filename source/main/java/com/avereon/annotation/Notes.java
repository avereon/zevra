package com.avereon.annotation;

import java.lang.annotation.*;

@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.SOURCE )
@Documented
@Inherited
public @interface Notes {

	Note[] value();

}
