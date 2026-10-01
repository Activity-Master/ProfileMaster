import com.guicedee.activitymaster.fsdm.client.services.systems.IMasterSystem;
import com.guicedee.activitymaster.profiles.ProfileSystem;
import com.guicedee.activitymaster.profiles.implementations.ProfileMasterModuleInclusion;
import com.guicedee.activitymaster.profiles.implementations.ProfileServiceBinder;
import com.guicedee.client.services.lifecycle.IGuiceModule;
import com.guicedee.client.services.config.IGuiceScanModuleInclusions;

module com.guicedee.activitymaster.profiles {

	//requires net.sf.uadetector.core;
	requires org.json;


	requires transitive com.guicedee.activitymaster.fsdm.client;
	requires com.guicedee.activitymaster.fsdm;
	requires static lombok;

    // REST surface + typed REST client for the comprehensive profile
    requires com.guicedee.rest;
    requires com.guicedee.rest.client;
    requires com.guicedee.openapi;

    // GraphQL schema provider
    requires com.guicedee.vertx.graphql;

    exports com.guicedee.activitymaster.profiles.dto;
	exports com.guicedee.activitymaster.profiles.exceptions;
	//exports com.guicedee.activitymaster.profiles.services;
	exports com.guicedee.activitymaster.profiles.services.interfaces;
	exports com.guicedee.activitymaster.profiles.services.enumerations;

	provides IMasterSystem with ProfileSystem;
	provides com.guicedee.activitymaster.fsdm.plugins.IAgeProfileProvider
			with com.guicedee.activitymaster.profiles.implementations.ProfileAgeProfileProvider;
	
	provides IGuiceModule with ProfileServiceBinder;
	//provides com.jwebmp.core.events.IEventConfigurator with ProfileEventConfigurator;
	provides IGuiceScanModuleInclusions with ProfileMasterModuleInclusion;
	provides com.guicedee.vertx.graphql.services.IGraphQLSchemaProvider
			with com.guicedee.activitymaster.profiles.implementations.graphql.ProfileGraphQLSchemaProvider;
	
	exports com.guicedee.activitymaster.profiles;
	
	opens com.guicedee.activitymaster.profiles to com.google.guice, com.guicedee.activitymaster.fsdm;
	opens com.guicedee.activitymaster.profiles.dto to  com.google.guice, tools.jackson.databind;
	opens com.guicedee.activitymaster.profiles.webdto to  com.google.guice, tools.jackson.databind;
	opens com.guicedee.activitymaster.profiles.deserializers to  com.google.guice, tools.jackson.databind;
	
	exports com.guicedee.activitymaster.profiles.implementations;
	opens com.guicedee.activitymaster.profiles.implementations to tools.jackson.databind, com.google.guice;
	
	exports com.guicedee.activitymaster.profiles.implementations.providers;
	opens com.guicedee.activitymaster.profiles.implementations.providers to tools.jackson.databind, com.google.guice;
	
	exports com.guicedee.activitymaster.profiles.implementations.updates;
	opens com.guicedee.activitymaster.profiles.implementations.updates to tools.jackson.databind, com.google.guice;

	exports com.guicedee.activitymaster.profiles.implementations.graphql;
	opens com.guicedee.activitymaster.profiles.implementations.graphql to com.google.guice;

	exports com.guicedee.activitymaster.profiles.enumerations;
	exports com.guicedee.activitymaster.profiles.webdto;
	exports com.guicedee.activitymaster.profiles.deserializers;

	exports com.guicedee.activitymaster.profiles.rest;
	opens com.guicedee.activitymaster.profiles.rest to com.google.guice, com.guicedee.rest, com.guicedee.rest.client, org.hibernate.reactive, net.bytebuddy, tools.jackson.databind;
}
