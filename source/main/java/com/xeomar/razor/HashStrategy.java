package com.xeomar.razor;

import java.io.InputStream;

public interface HashStrategy {

	String hash( InputStream input );

}
