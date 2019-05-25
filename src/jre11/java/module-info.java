module com.armineasy.activitymaster.profiles {
	requires com.armineasy.activitymaster.activitymaster;
	requires com.google.guice;

	requires com.jwebmp.guicedinjection;
	requires com.google.common;
	requires javax.servlet.api;


	requires lombok;
	requires org.mapstruct;
	requires net.sf.uadetector.core;
	requires org.json;
	requires com.jwebmp.guicedpersistence;
	requires com.jwebmp.guicedservlets;

	requires cache.annotations.ri.common;
	requires cache.annotations.ri.guice;
	requires cache.api;

	exports com.armineasy.activitymaster.profiles.dto;
	exports com.armineasy.activitymaster.profiles.services;
	exports com.armineasy.activitymaster.profiles.services.interfaces;

	provides com.armineasy.activitymaster.activitymaster.services.IActivityMasterSystem with com.armineasy.activitymaster.profiles.ProfileSystem;

	opens com.armineasy.activitymaster.profiles to com.google.guice, com.armineasy.activitymaster.activitymaster;
	opens com.armineasy.activitymaster.profiles.events.visits to com.google.guice, com.armineasy.activitymaster.activitymaster;
	opens com.armineasy.activitymaster.profiles.events to com.google.guice, com.armineasy.activitymaster.activitymaster;
	opens com.armineasy.activitymaster.profiles.dto to com.fasterxml.jackson.databind;
}
