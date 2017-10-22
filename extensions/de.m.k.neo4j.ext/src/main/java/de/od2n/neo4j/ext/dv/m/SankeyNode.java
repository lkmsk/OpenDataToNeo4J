package de.od2n.neo4j.ext.dv.m;

import javax.json.bind.annotation.JsonbProperty;

public class SankeyNode {

	@JsonbProperty("node")
	public int withId;

	@JsonbProperty("name")
	public String withName;

	public SankeyNode withId(int id) {
		this.withId = id;
		return this;
	}

	public SankeyNode withName(String name) {
		this.withName = name;
		return this;
	}
}
