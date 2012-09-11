package com.kdcloud.server.rest.resource;

import org.restlet.Request;

import com.kdcloud.server.domain.datastore.User;
import com.kdcloud.server.persistence.PersistenceContext;

public interface UserProvider {
	
	public User getUser(Request request, PersistenceContext pc);

}
