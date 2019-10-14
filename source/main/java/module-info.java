open module com.avereon.zevra {

	requires java.logging;
	requires java.management;
	requires java.xml;
	requires jdk.management;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires org.slf4j;
	requires com.fasterxml.jackson.annotation;

	exports com.avereon.product;
	exports com.avereon.settings;
	exports com.avereon.undo;
	exports com.avereon.util;
}
