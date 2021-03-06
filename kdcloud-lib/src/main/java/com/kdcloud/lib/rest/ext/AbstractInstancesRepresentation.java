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
package com.kdcloud.lib.rest.ext;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.representation.CharacterRepresentation;
import org.restlet.representation.Representation;

import weka.core.Instances;
import weka.core.converters.Loader;

public abstract class AbstractInstancesRepresentation extends CharacterRepresentation {
	
	private Instances instances;
	private Representation representation;
	
	public AbstractInstancesRepresentation(MediaType mediaType) {
		super(mediaType);
	}
	
	public AbstractInstancesRepresentation(MediaType mediaType,
			Instances instances) {
		super(mediaType);
		this.instances = instances;
	}

	public AbstractInstancesRepresentation(Representation representation) {
		super(representation.getMediaType());
		this.representation = representation;
	}

	protected abstract Loader getLoader() throws IOException;
	
	public void setInstances(Instances instances) {
		this.instances = instances;
	}

	public Instances getInstances() throws IOException {
		if (instances == null) {
			if (representation == null)
				throw new IOException("instances is null");
			if (representation instanceof AbstractInstancesRepresentation)
				instances = ((AbstractInstancesRepresentation) representation)
						.getInstances();
			else {
				Loader loader = getLoader();
				loader.setSource(representation.getStream());
				instances = loader.getDataSet();
			}
		}
		return instances;
	}

}
