import com.avereon.log.java.JavaLoggingProvider;
import com.avereon.log.provider.LoggingProvider;

module com.avereon.zevra {

	requires static java.logging;
	requires static java.management;
	requires static java.xml;
	requires static jdk.management;
	//requires static jsr305;
	requires static lombok;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
	requires me.xdrop.fuzzywuzzy;
	//requires org.jsoup;
	requires org.jspecify;

	opens com.avereon.util to com.fasterxml.jackson.databind;

	exports com.avereon.annotation;
	exports com.avereon.data;
	exports com.avereon.event;
	exports com.avereon.index;
	exports com.avereon.log;
	exports com.avereon.log.provider;
	exports com.avereon.product;
	exports com.avereon.result;
	exports com.avereon.settings;
	exports com.avereon.skill;
	exports com.avereon.test;
	exports com.avereon.transaction;
	exports com.avereon.util;

	uses LoggingProvider;

	provides LoggingProvider with JavaLoggingProvider;
}
