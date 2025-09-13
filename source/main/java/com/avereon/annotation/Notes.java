package com.avereon.annotation;

import java.lang.annotation.*;

@Target( { ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.CONSTRUCTOR } )
@Retention( RetentionPolicy.SOURCE )
@Documented
@Inherited
public @interface Notes {

	Note[] value();

}
