import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.implementations.*;

module com.guicedee.activitymaster.profiles {

	requires static lombok;

	requires com.guicedee.activitymaster.core;
	requires com.google.guice;

	requires com.guicedee.guicedinjection;
	requires com.google.common;

	requires com.fasterxml.jackson.databind;

	requires net.sf.uadetector.core;
	requires org.json;
	requires com.guicedee.guicedpersistence;
	requires com.guicedee.guicedservlets;

	requires cache.annotations.ri.common;
	requires cache.annotations.ri.guice;
	requires cache.api;
	requires com.fasterxml.jackson.annotation;
	requires io.github.classgraph;
	requires com.jwebmp.core;
	
	
	requires jakarta.validation;

	exports com.guicedee.activitymaster.profiles.dto;
	exports com.guicedee.activitymaster.profiles.exceptions;
	//exports com.guicedee.activitymaster.profiles.services;
	exports com.guicedee.activitymaster.profiles.services.interfaces;
	exports com.guicedee.activitymaster.profiles.services.enumerations;

	provides com.guicedee.activitymaster.core.services.IActivityMasterSystem with ProfileSystem;
	provides com.guicedee.guicedinjection.interfaces.IGuiceModule with ProfileServiceBinder;
	provides com.jwebmp.core.events.IEventConfigurator with ProfileEventConfigurator;
	provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with ProfileMasterModuleInclusion;
	
	opens com.guicedee.activitymaster.profiles to com.google.guice, com.guicedee.activitymaster.core;
	opens com.guicedee.activitymaster.profiles.events.visits to com.google.guice, com.guicedee.activitymaster.core;
	opens com.guicedee.activitymaster.profiles.events to com.google.guice, com.guicedee.activitymaster.core;
	opens com.guicedee.activitymaster.profiles.dto to  com.google.guice, com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.profiles.webdto to  com.google.guice, com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.profiles.deserializers to  com.google.guice, com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.profiles.implementations to com.fasterxml.jackson.databind, com.google.guice;
	opens com.guicedee.activitymaster.profiles.implementations.providers to com.fasterxml.jackson.databind, com.google.guice;
	opens com.guicedee.activitymaster.profiles.implementations.updates to com.fasterxml.jackson.databind, com.google.guice;

	exports com.guicedee.activitymaster.profiles.enumerations;
	exports com.guicedee.activitymaster.profiles.events;
	exports com.guicedee.activitymaster.profiles.webdto;
	exports com.guicedee.activitymaster.profiles.deserializers;
	exports com.guicedee.activitymaster.profiles.events.visits;
}
