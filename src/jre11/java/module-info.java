import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.implementations.ProfileEventConfigurator;
import com.guicedee.activitymaster.profiles.implementations.ProfileServiceBinder;

module com.guicedee.activitymaster.profiles {
	requires com.guicedee.activitymaster.core;
	requires com.google.guice;

	requires com.guicedee.guicedinjection;
	requires com.google.common;
	requires javax.servlet.api;

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
	requires java.validation;
	requires com.guicedee.activitymaster.sessions;

	exports com.guicedee.activitymaster.profiles.dto;
	exports com.guicedee.activitymaster.profiles.exceptions;
	//exports com.guicedee.activitymaster.profiles.services;
	exports com.guicedee.activitymaster.profiles.services.interfaces;
	exports com.guicedee.activitymaster.profiles.services.enumerations;

	provides com.guicedee.activitymaster.core.services.IActivityMasterSystem with ProfileSystem;
	provides com.guicedee.guicedinjection.interfaces.IGuiceModule with ProfileServiceBinder;
	provides com.jwebmp.core.events.IEventConfigurator with ProfileEventConfigurator;

	opens com.guicedee.activitymaster.profiles to com.google.guice, com.guicedee.activitymaster.core;
	opens com.guicedee.activitymaster.profiles.events.visits to com.google.guice, com.guicedee.activitymaster.core;
	opens com.guicedee.activitymaster.profiles.events to com.google.guice, com.guicedee.activitymaster.core;
	opens com.guicedee.activitymaster.profiles.dto to com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.profiles.webdto to com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.profiles.deserializers to com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.profiles.implementations to com.fasterxml.jackson.databind,com.google.guice;

	exports com.guicedee.activitymaster.profiles.enumerations;
	exports com.guicedee.activitymaster.profiles.events;
	exports com.guicedee.activitymaster.profiles.webdto;
}
