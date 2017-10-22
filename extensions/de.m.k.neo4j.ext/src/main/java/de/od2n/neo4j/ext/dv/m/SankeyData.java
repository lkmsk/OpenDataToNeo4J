package de.od2n.neo4j.ext.dv.m;

import java.util.ArrayList;
import java.util.List;

import javax.json.bind.annotation.JsonbProperty;

public class SankeyData {

	@JsonbProperty("diagramTitle")
	public String titel = "Sankey";

	@JsonbProperty("links")
	public List<SankeyLink> linkList = new ArrayList<SankeyLink>();

	@JsonbProperty("nodes")
	public List<SankeyNode> nodeList = new ArrayList<SankeyNode>();

	public void addNodeToSankey(SankeyNode node) {
		this.nodeList.add(node);
	}

	public void addLinkToSankey(SankeyLink link) {
		this.linkList.add(link);
	}

	public void addNodeListToSankey(List<SankeyNode> list) {
		this.nodeList.addAll(list);
	}

	public void addLinkListToSankey(List<SankeyLink> list) {
		this.linkList.addAll(list);
	}
}
