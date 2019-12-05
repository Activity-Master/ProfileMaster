package com.guicedee.activitymaster.profiles.implementations;

import com.jwebmp.core.Event;

import javax.validation.constraints.NotNull;

public class ProfileEventConfigurator implements com.jwebmp.core.events.IEventConfigurator<ProfileEventConfigurator>
{

	@Override
	public @NotNull Event<?, ?> configureEvent(Event<?, ?> event)
	{
		event.returnVariable("user");
		return event;
	}

}
