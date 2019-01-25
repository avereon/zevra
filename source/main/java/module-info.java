module com.xeomar.zenna {

	requires java.logging;
	requires java.management;
	requires java.xml;
	requires jdk.management;
	requires org.apache.commons.io;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires org.slf4j;
	requires com.fasterxml.jackson.annotation;

	exports com.xeomar.product;
	exports com.xeomar.settings;
	exports com.xeomar.undo;
	exports com.xeomar.util;
}
