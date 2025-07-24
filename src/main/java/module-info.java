import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.implementations.ProfileMasterModuleInclusion;
import com.guicedee.activitymaster.profiles.implementations.ProfileServiceBinder;

module com.guicedee.activitymaster.profiles {
	requires transitive com.guicedee.guicedinjection;

	//requires net.sf.uadetector.core;
	requires org.json;

	requires com.fasterxml.jackson.annotation;
	requires io.github.classgraph;

	requires transitive com.guicedee.jsonrepresentation;
	requires transitive com.guicedee.activitymaster.fsdm.client;
	requires static lombok;
    requires org.hibernate.reactive;

    exports com.guicedee.activitymaster.profiles.dto;
	exports com.guicedee.activitymaster.profiles.exceptions;
	//exports com.guicedee.activitymaster.profiles.services;
	exports com.guicedee.activitymaster.profiles.services.interfaces;
	exports com.guicedee.activitymaster.profiles.services.enumerations;

	provides IActivityMasterSystem with ProfileSystem;
	
	provides com.guicedee.guicedinjection.interfaces.IGuiceModule with ProfileServiceBinder;
	//provides com.jwebmp.core.events.IEventConfigurator with ProfileEventConfigurator;
	provides com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions with ProfileMasterModuleInclusion;
	
	exports com.guicedee.activitymaster.profiles;
	
	opens com.guicedee.activitymaster.profiles to com.google.guice, com.guicedee.activitymaster.fsdm;
	opens com.guicedee.activitymaster.profiles.dto to  com.google.guice, com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.profiles.webdto to  com.google.guice, com.fasterxml.jackson.databind;
	opens com.guicedee.activitymaster.profiles.deserializers to  com.google.guice, com.fasterxml.jackson.databind;
	
	exports com.guicedee.activitymaster.profiles.implementations;
	opens com.guicedee.activitymaster.profiles.implementations to com.fasterxml.jackson.databind, com.google.guice;
	
	exports com.guicedee.activitymaster.profiles.implementations.providers;
	opens com.guicedee.activitymaster.profiles.implementations.providers to com.fasterxml.jackson.databind, com.google.guice;
	
	exports com.guicedee.activitymaster.profiles.implementations.updates;
	opens com.guicedee.activitymaster.profiles.implementations.updates to com.fasterxml.jackson.databind, com.google.guice;

	exports com.guicedee.activitymaster.profiles.enumerations;
	exports com.guicedee.activitymaster.profiles.webdto;
	exports com.guicedee.activitymaster.profiles.deserializers;
}
