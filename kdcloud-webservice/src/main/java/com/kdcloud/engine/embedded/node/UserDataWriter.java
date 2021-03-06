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
package com.kdcloud.engine.embedded.node;

import java.util.HashSet;
import java.util.Set;

import com.kdcloud.engine.embedded.BufferedInstances;
import com.kdcloud.engine.embedded.NodeAdapter;
import com.kdcloud.engine.embedded.WorkerConfiguration;
import com.kdcloud.engine.embedded.WrongConfigurationException;
import com.kdcloud.engine.embedded.WrongInputException;
import com.kdcloud.server.entity.DataTable;
import com.kdcloud.server.entity.Group;
import com.kdcloud.server.entity.User;
import com.kdcloud.server.persistence.EntityMapper;
import com.kdcloud.server.persistence.InstancesMapper;

public class UserDataWriter extends NodeAdapter {
	
	public static final String DEST_USER_PARAMETER = "destinationUser";
	public static final String DEST_GROUP_PARAMETER = "destinationGroup";

	BufferedInstances mState;
	Group group;
	User user;
	EntityMapper entityMapper;
	InstancesMapper instancesMapper;
	
	public UserDataWriter() {
		// TODO Auto-generated constructor stub
	}
	

	public UserDataWriter(User user) {
		super();
		this.user = user;
	}

	@Override
	public void configure(WorkerConfiguration config) throws WrongConfigurationException  {
		String msg = null;
		String userId = (String) config.get(DEST_USER_PARAMETER);
		String groupId = (String) config.get(DEST_GROUP_PARAMETER);
		entityMapper = (EntityMapper) config.get(EntityMapper.class.getName());
		instancesMapper = (InstancesMapper) config.get(InstancesMapper.class.getName());
		if (entityMapper == null)
			msg = "no persistence context in configuration";
		if (userId != null)
			user = (User) entityMapper.findByName(User.class, userId);
		if (user == null)
			msg = "not a valid user in configuration";
		if (groupId != null)
			group = (Group) entityMapper.findByName(Group.class, groupId);
		if (group == null)
			msg = "not a valid group in configuration";
		if (msg != null)
			throw new WrongConfigurationException(msg);
	}

	@Override
	public Set<String> getParameters() {
		Set<String> params = new HashSet<String>();
		if (user == null)
			params.add(DEST_USER_PARAMETER);
		if (group == null)
			params.add(DEST_GROUP_PARAMETER);
		return params;
	}

	@Override
	public void setInput(BufferedInstances input) throws WrongInputException {
		if (input instanceof BufferedInstances) {
			mState = (BufferedInstances) input;
		} else {
			throw new WrongInputException();
		}
	}

	@Override
	public void run() {
		DataTable t = new DataTable(user);
		group.getData().add(t);
		entityMapper.save(group);
		instancesMapper.save(mState.getInstances(), t);
	}

}
