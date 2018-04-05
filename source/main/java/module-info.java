module com.xeomar.razor {

	requires java.logging;
	requires java.management;
	requires java.xml;
	requires jdk.management;
	requires commons.io;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires jackson.annotations;
	requires org.slf4j;

	exports com.xeomar.util;
	exports com.xeomar.product;
	exports com.xeomar.settings;

}