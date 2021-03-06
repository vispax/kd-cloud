/**
 * Copyright (C) 2012, Vincenzo Pirrone <pirrone.v@gmail.com>

 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */
package com.kdcloud.server.entity;

import java.util.LinkedList;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class User extends Entity {
	
	
	public User() {
		super();
	}

	public User(String name) {
		super(name);
	}

	@Persistent(serialized = "true")
	private LinkedList<String> devices = new LinkedList<String>();
	
	@Persistent(mappedBy="applicant")
	private List<Task> submittedTasks = new LinkedList<Task>();
	
	public LinkedList<String> getDevices() {
		return devices;
	}

	public void setDevices(LinkedList<String> devices) {
		this.devices = devices;
	}
	
	public boolean isOwner(Describable entity) {
		return equals(entity.getOwner());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		else if (obj instanceof User)
			return ((User) obj).getName().equals(this.getName());
		return false;
	}

	public List<Task> getSubmittedTasks() {
		return submittedTasks;
	}

	public void setSubmittedTasks(List<Task> submittedTasks) {
		this.submittedTasks = submittedTasks;
	}
	

}
