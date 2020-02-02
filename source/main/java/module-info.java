module com.avereon.zevra {

	requires static java.logging;
	requires static java.management;
	requires static java.xml;
	requires static jdk.management;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	// This transitive dependency causes a compiler warning because slf4j is still
	// an automatic module. The curex maven plugin solves this at assembly time
	// but the warning will persist at compile time.
	// Deprecated
	requires transitive org.slf4j;

	exports com.avereon.event;
	exports com.avereon.product;
	exports com.avereon.settings;
	exports com.avereon.undo;
	exports com.avereon.util;

}
