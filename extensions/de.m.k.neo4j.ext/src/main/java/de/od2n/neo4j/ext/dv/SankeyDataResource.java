package de.od2n.neo4j.ext.dv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;

import de.od2n.neo4j.ext.dv.m.SankeyData;
import de.od2n.neo4j.ext.dv.m.SankeyLink;
import de.od2n.neo4j.ext.dv.m.SankeyNode;

@Path("/visualizationData")
public class SankeyDataResource {

	private GraphDatabaseService databaseService;
	private Map<String, SankeyLink> mapOfLinks = new HashMap<String, SankeyLink>();
	private Map<String, Integer> nodeIdMap = new HashMap<String, Integer>();
	private int nodeIdCounter;
	
	@Context Log log;

	public SankeyDataResource(@Context GraphDatabaseService databaseService) {
		this.databaseService = databaseService;
	}

	@GET
	@Produces( MediaType.APPLICATION_JSON )
	@Path("/sankeyData/{label}/{filterType}/{count}")
	public Response getSankeyData(
			@PathParam("label") String label
			, @PathParam("filterType") String filterType
			, @DefaultValue("0") @PathParam("count") int count) {
		log.info("[getSankeyData] generate JSON");
		log.info("Parameter for call: " + label + "|" + filterType + "|" + count);
		StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				try (
					Transaction tx 
						= databaseService.beginTx();
					Result result 
						= databaseService
							.execute
								(queryForGettingSummaryInformations(label))
					) {
					/*
					Supplier<Stream<Map<String, Object>>> supplier = () -> result.stream();
					log.info("[getSankeyData] result as string: " 
		                    + supplier
		                        .get()
		                          .map(
		                            mi -> mi.entrySet()
		                                      .stream()
		                                        .map(es -> es.getKey() + ": " + es.getValue().toString())
		                                        .collect(Collectors.joining(" | "))
		                            )
		                          .collect(Collectors.joining(",")));
		                          */
					
					if(result == null) {
	                    log.error("[getSankeyData] Result is null");
	                    return;
	                } else {
	                    log.info("[getSankeyData] state after execute {tx: " + tx.toString() + "} { result: " + result.getQueryStatistics() + "}");
	                    nodeIdCounter = 0;
						SankeyData sankeyData = null;
						List<SankeyLink> listOfLinks = null;
						Map<String, SankeyLink> filteredMapOfLinks = null;
						List<SankeyNode> sortedListOfNodes = null;
						
						while (result.hasNext()) {
							Map<String, Object> row 
								= result.next();
							@SuppressWarnings("unchecked")
							Map<String, Object> mapOfPaths 
								= (Map<String, Object>) row.get("mapOfPaths");
							if (mapOfPaths != null) {
								log.info("Number of map items: " + mapOfPaths.entrySet().size());
								Iterator<Entry<String, Object>> it 
									= mapOfPaths.entrySet().iterator();
								while (it.hasNext()) {
									processMapOfPathsEntry
										(it.next(), filterType, count);
								}
							}
						}
						
						log.info("[getSankeyData] filter map of links ");
						
						filteredMapOfLinks 
							= mapOfLinks.entrySet().stream()
								.filter(
									(l) -> l.getValue()
										.valueIsInclusive(filterType, count)
								)
								.collect(
									Collectors.toMap
										(Entry<String, SankeyLink>::getKey, 
										Entry<String, SankeyLink>::getValue)
								);
						
						log.info("[getSankeyData] create node list ");
						
					    List<SankeyNode> nodeList =
				    	    new ArrayList<SankeyNode>();
		
			    	    nodeIdMap
			    	      .keySet()
			    	      .stream()
			    	      .forEach(
			    	        (key) ->
			    	        nodeList
			    	          .add(new SankeyNode()
			    	            .withName(key)
			    	            .withId(nodeIdMap.get(key))));
			    	    
			    	    log.info("[getSankeyData] sort node list ");
	
			    	    sortedListOfNodes =
			    	      nodeList
			    	        .stream()
			    	        .sorted(
			    	          (n1, n2) ->
			    	            Integer.compare(n1.withId, n2.withId))
			    	        .collect(Collectors.toList());

			    	    log.info("[getSankeyData] create sankeyData ");
			    	    
			    	    sankeyData = new SankeyData();
			    	    
			    	    listOfLinks
			    	    	= new ArrayList<SankeyLink>(
			    	    		filteredMapOfLinks.values());
	
			    	    sankeyData.addLinkListToSankey(listOfLinks);
			    	    sankeyData.addNodeListToSankey(sortedListOfNodes);
			    	    
						try {
							JsonbConfig config = new JsonbConfig().withNullValues(true);
							Jsonb jsonb = JsonbBuilder.create(config);
							String bResult = jsonb.toJson(sankeyData);
							
							log.info("[getSankeyData] sankeydata as json: " + bResult);
							
							os.write(bResult.getBytes("UTF-8"));
						} catch (Exception e) {
							e.printStackTrace();
							String error = "{ errorTitle:\"Error occurred:\", errorMsg:\""
									+ e.getMessage() + "\"}";
							os.write(error.getBytes("UTF-8"));
						}
	                }
				}
			}

		};
		return Response.ok().entity(stream).type(MediaType.APPLICATION_JSON).build();
	}

	private String queryForGettingSummaryInformations(String label) {
		return "MATCH (s:" + label + ") " + " RETURN apoc.map.fromLists "
				+ "(s.listOfLabels,s.listOfCounts) as mapOfPaths";
	}

	protected void processMapOfPathsEntry(Entry<String, Object> e, String filterType, int count) {
		
		log.info("[getSankeyData] process entry " + e.getKey() + " [" + e.getValue() + "]");
		
		long value = Long.parseLong(e.getValue().toString());

		String[] pathMembers = e.getKey().toString().split(Pattern.quote("|"));

		for (int i = pathMembers.length - 1; i > 0; i--) {
			String targetMember = pathMembers[i];
			String sourceMember = pathMembers[i - 1];
			String keyForLink = sourceMember + "#" + targetMember;
			
			log.info("[getSankeyData] process link " +  sourceMember + "|" + targetMember);
			
			nodeIdMap.computeIfAbsent(targetMember, (k) -> nodeIdCounter++);
			nodeIdMap.computeIfAbsent(sourceMember, (k) -> nodeIdCounter++);

			mapOfLinks.computeIfPresent(keyForLink, (k, link) -> {
				link.increaseValue(value);
				return link;
			});

			mapOfLinks.computeIfAbsent(keyForLink, (k) -> {
				int targetNodeId = nodeIdMap.get(targetMember);
				int sourceNodeId = nodeIdMap.get(sourceMember);
				SankeyLink link = 
					new SankeyLink()
						.withSrcNodeId(sourceNodeId)
						.withTrgNodeId(targetNodeId)
						.withValue(value);

				return link;
			});
		}
	}
}
