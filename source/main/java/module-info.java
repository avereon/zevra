module com.xeomar.razor {

	requires org.slf4j;
	requires commons.io;
	requires java.logging;
	requires java.management;
	requires jdk.management;
	requires java.xml;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires jackson.annotations;

	exports com.xeomar.util;
	exports com.xeomar.product;
	exports com.xeomar.settings;

}