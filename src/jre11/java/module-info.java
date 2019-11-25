import com.armineasy.activitymaster.profiles.implementations.ProfileServiceBinder;

module com.armineasy.activitymaster.profiles {
	requires com.armineasy.activitymaster.activitymaster;
	requires com.google.guice;

	requires com.guicedee.guicedinjection;
	requires com.google.common;
	requires javax.servlet.api;

	requires com.fasterxml.jackson.databind;

	requires org.mapstruct;
	requires net.sf.uadetector.core;
	requires org.json;
	requires com.guicedee.guicedpersistence;
	requires com.jwebmp.guicedservlets;

	requires cache.annotations.ri.common;
	requires cache.annotations.ri.guice;
	requires cache.api;
	requires com.fasterxml.jackson.annotation;
	requires io.github.classgraph;
	requires com.jwebmp.core;
	requires java.validation;

	exports com.armineasy.activitymaster.profiles.dto;
	exports com.armineasy.activitymaster.profiles.exceptions;
	//exports com.armineasy.activitymaster.profiles.services;
	exports com.armineasy.activitymaster.profiles.services.interfaces;
	exports com.armineasy.activitymaster.profiles.services.enumerations;

	provides com.armineasy.activitymaster.activitymaster.services.IActivityMasterSystem with com.armineasy.activitymaster.profiles.ProfileSystem;
	provides com.guicedee.guicedinjection.interfaces.IGuiceModule with ProfileServiceBinder;
	provides com.jwebmp.core.events.IEventConfigurator with com.armineasy.activitymaster.profiles.implementations.ProfileEventConfigurator;

	opens com.armineasy.activitymaster.profiles to com.google.guice, com.armineasy.activitymaster.activitymaster;
	opens com.armineasy.activitymaster.profiles.events.visits to com.google.guice, com.armineasy.activitymaster.activitymaster;
	opens com.armineasy.activitymaster.profiles.events to com.google.guice, com.armineasy.activitymaster.activitymaster;
	opens com.armineasy.activitymaster.profiles.dto to com.fasterxml.jackson.databind;
	opens com.armineasy.activitymaster.profiles.deserializers to com.fasterxml.jackson.databind;
	opens com.armineasy.activitymaster.profiles.implementations to com.fasterxml.jackson.databind,com.google.guice;

	exports com.armineasy.activitymaster.profiles.enumerations;
	exports com.armineasy.activitymaster.profiles.events;
}
