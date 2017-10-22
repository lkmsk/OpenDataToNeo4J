package de.od2n.neo4j.ext.dv.m;

import javax.json.bind.annotation.JsonbProperty;

public class SankeyLink {

	@JsonbProperty("source")
	public int withSrcNodeId;

	@JsonbProperty("target")
	public int withTrgNodeId;

	@JsonbProperty("value")
	public long withValue;

	public SankeyLink withSrcNodeId(int id) {
		this.withSrcNodeId = id;
		return this;
	}

	public SankeyLink withTrgNodeId(int id) {
		this.withTrgNodeId = id;
		return this;
	}

	public SankeyLink withValue(long value) {
		this.withValue = value;
		return this;
	}

	public boolean valueIsInclusive(String filterType, int count) {
		boolean inclusive = false;
		switch (filterType.toLowerCase()) {
		case "min":
			inclusive = withValue >= count;
			break;
		case "max":
			inclusive = withValue <= count;
			break;
		}
		return inclusive;
	}
	
	public long increaseValue(long value) {
		this.withValue += value;
		return this.withValue;
	}

}
