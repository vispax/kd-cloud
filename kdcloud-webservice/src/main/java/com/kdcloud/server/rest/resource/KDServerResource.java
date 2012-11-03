/**
 * Copyright (C) 2012 Vincenzo Pirrone
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.kdcloud.server.rest.resource;

import org.restlet.Application;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.kdcloud.server.entity.User;
import com.kdcloud.server.persistence.PersistenceContext;
import com.kdcloud.server.persistence.PersistenceContextFactory;

public abstract class KDServerResource extends ServerResource {

	private PersistenceContext persistenceContext;
	private UserProvider userProvider;
	private String resourceIdentifier;

	User user;

	public KDServerResource() {
	}

	KDServerResource(Application application, String resourceIdentifier) {
		setApplication(application);
		doInit();
		this.user = userProvider.getUser(null, persistenceContext);
		this.resourceIdentifier = resourceIdentifier;
	}

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		userProvider = (UserProvider) inject(UserProvider.class);

		PersistenceContextFactory pcf = 
				(PersistenceContextFactory) inject(PersistenceContextFactory.class);
		persistenceContext = pcf.get();
	}

	protected String getResourceIdentifier() {
		if (getRequest() != null)
			return (String) getRequestAttributes().get("id");
		return resourceIdentifier;
	}
	
	protected String getActualUri(String template) {
		return template.replace("{id}", getResourceIdentifier());
	}
	
	public Representation fetchLocalResource(String path) {
		LocalReference ref = new LocalReference(path);
		ref.setProtocol(Protocol.CLAP);
		return new ClientResource(ref).get();
	}
	
	public Representation doGet() {
		if (getRequest() != null) {
			ClientResource cr = new ClientResource(getRequest().getResourceRef());
			cr.setChallengeResponse(getRequest().getChallengeResponse());
			return cr.get();
		} else {
			return fetchLocalResource(getResourceIdentifier());
		}
	}


	@Override
	public Representation handle() {
		user = userProvider.getUser(getRequest(), persistenceContext);
		Representation representation = super.handle();
		return representation;
	}

	protected Object inject(Class<?> baseClass) {
		return getApplication().getContext().getAttributes()
				.get(baseClass.getName());
	}

	public PersistenceContext getPersistenceContext() {
		return persistenceContext;
	}

}
