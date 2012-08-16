package com.kdcloud.server.rest.resource;

import java.util.ArrayList;

import org.restlet.resource.Get;
import org.restlet.resource.Put;

import com.kdcloud.server.entity.DataTable;
import com.kdcloud.server.entity.Dataset;
import com.kdcloud.server.rest.api.DataTableResource;

public class DataTableServerResource extends KDServerResource implements DataTableResource {
 
	
	@Override
	@Put
	public Long createDataset(String name, String description) {
		DataTable dataset = new DataTable();
		dataset.getCommitters().add(getUserId());
		user.getTables().add(dataset);
		userDao.save(user);
		return dataset.getId();
	}

	@Override
	@Get
	public ArrayList<Dataset> list() {
		ArrayList<Dataset> list = new ArrayList<Dataset>(user.getTables().size());
		for (DataTable table : user.getTables()) {
			Dataset dto = new Dataset();
			dto.setName(table.getName());
			dto.setDescription(table.getDescription());
			dto.setSize(table.getDataRows().size());
			dto.setId(table.getId());
			list.add(dto);
		}
		return list;
	}

}
