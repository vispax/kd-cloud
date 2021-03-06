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
package com.kdcloud.engine.embedded.model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.kdcloud.engine.embedded.Node;
import com.kdcloud.engine.embedded.NodeLoader;

public class NodeDescription {
	
	public static final String NODE_PACKAGE = "com.kdcloud.engine.embedded.node";

	public NodeDescription() {
		// TODO Auto-generated constructor stub
	}

	public NodeDescription(Class<? extends Node> type) {
		super();
		this.type = type.getSimpleName();
	}
	
	@XmlElement(required=true)
	String type;

	@XmlElement(name = "parameter")
	List<InitParam> parameters = new LinkedList<NodeDescription.InitParam>();

	static class InitParam {

		public String name;
		public String value;
	}

	public Node create(NodeLoader nodeLoader) throws IOException {
		try {
			String className = NODE_PACKAGE + "." + type;
			Class<? extends Node> clazz = nodeLoader.loadNode(className);
			Node node = clazz.newInstance();
			for (InitParam p : parameters) {
				String setter = "set" + Character.toUpperCase(p.name.charAt(0))
						+ p.name.substring(1);
				clazz.getMethod(setter, String.class).invoke(node, p.value);
			}
			return node;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("error creating node " + type);
		}
	}

}
