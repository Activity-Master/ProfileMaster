package com.armineasy.activitymaster.profiles;

import com.armineasy.activitymaster.activitymaster.db.entities.security.SecurityToken;
import com.armineasy.activitymaster.activitymaster.db.hierarchies.SecurityHierarchyView;
import com.armineasy.activitymaster.activitymaster.services.dto.IEnterprise;
import com.armineasy.activitymaster.activitymaster.services.system.ISecurityTokenService;
import com.google.inject.Singleton;
import com.jwebmp.guicedinjection.GuiceContext;
import lombok.extern.java.Log;

import java.util.UUID;

@Singleton
@Log
public class ProfileCountersService
{
	public Long getNumberOfVisitors(IEnterprise<?> enterprise, UUID... identityToken)
	{
		ISecurityTokenService securityTokenService = GuiceContext.get(ISecurityTokenService.class);
		SecurityToken st = securityTokenService.getVisitorsGuestsFolder(enterprise, identityToken);
		SecurityHierarchyView hierarchyView = new SecurityHierarchyView();
		return hierarchyView.builder()
		                    .findMyChildren(st.getId())
		                    .getCount();
	}

	public Long getNumberOfRegistered(IEnterprise<?> enterprise, UUID... identityToken)
	{
		ISecurityTokenService securityTokenService = GuiceContext.get(ISecurityTokenService.class);
		SecurityToken st = securityTokenService.getRegisteredGuestsFolder(enterprise, identityToken);
		SecurityHierarchyView hierarchyView = new SecurityHierarchyView();
		return hierarchyView.builder()
		                    .findMyChildren(st.getId())
		                    .getCount();
	}


}
