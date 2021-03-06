import com.avereon.log.java.JavaLoggingProvider;
import com.avereon.log.provider.LoggingProvider;

module com.avereon.zevra {

	requires static java.logging;
	requires static java.management;
	requires static java.xml;
	requires static jdk.management;
	requires static lombok;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;

	exports com.avereon.data;
	exports com.avereon.event;
	exports com.avereon.log;
	exports com.avereon.product;
	exports com.avereon.settings;
	exports com.avereon.skill;
	exports com.avereon.transaction;
	exports com.avereon.util;

	uses LoggingProvider;

	provides LoggingProvider with JavaLoggingProvider;
}
