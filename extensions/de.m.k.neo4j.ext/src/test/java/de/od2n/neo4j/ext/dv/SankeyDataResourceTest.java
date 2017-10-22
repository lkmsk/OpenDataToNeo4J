package de.od2n.neo4j.ext.dv;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.Result;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.shell.kernel.apps.cypher.Cypher;
import org.neo4j.test.server.HTTP;

import apoc.map.Maps;
import apoc.meta.Meta;

public class SankeyDataResourceTest {
	
	private static final String basePath = "C:\\Development\\workspaces\\graphdb.extensions\\run";

	private static final String serverPath = basePath + "\\neo4j";

	private static final String logsPath = basePath + "\\logs";
	
	private final String testLogPrefix = "[SankeyDataResourceTest]";
	  private final String testLogFrame = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
	  private final String testLogFrameTitlePart = "~~~~~~~~~~~~~";
	  private static final String unmanagedExtensionsPath = "/ext/unmanaged";
	  private static final List<Integer> listOfCounts = Arrays.asList( 20, 26, 32, 48, 155, 75, 5, 25);
	  private static final List<String> listOfLabels 
	  	= Arrays.asList(
	  			"BOOKING|MITTELKLASSE|UNKNOWN_TICHT|UNKNOWN_TICHG|LONG_DISTANCE"
	  			,"BOOKING|MITTELKLASSE|UNKNOWN_TICHT|UNKNOWN_TICHG|MIDDLE_DISTANCE"
	  			,"BOOKING|MITTELKLASSE|DB|LONG_DISTANCE"
	  			,"BOOKING|KOMPAKTKLASSE|DB|MIDDLE_DISTANCE"
	  			,"BOOKING|MINI|UNKNOWN_TICHT|UNKNOWN_TICHG|SHORT_DISTANCE"
	  			,"BOOKING|VAN_BUSKLASSE|APP|FLINKSTER|MIDDLE_DISTANCE"
	  			,"BOOKING|VAN_BUSKLASSE|APP|FLINKSTER|LONG_DISTANCE"
	  			,"BOOKING|VAN_BUSKLASSE|INTERNET|DB|SHORT_DISTANCE"
	  	);

	  public SankeyDataResourceTest () {
	    // do nothing
	  }
	  @Rule
	  public Neo4jRule neo4j = new Neo4jRule(
			  new File(
                  SankeyDataResourceTest.serverPath
                )
			  )
	  	.withConfig("dbms.unmanaged_extension_classes", "de.od2n.neo4j.ext.dv=" + unmanagedExtensionsPath)
	    .withConfig("dbms.security.procedures.unrestricted", "apoc.*,de.od2n.*")
	    .withConfig("dbms.directories.logs", SankeyDataResourceTest.logsPath)
	    .withConfig("dbms.directories.run", SankeyDataResourceTest.basePath)
	    .withConfig("dbms.logs.debug.level", "DEBUG")
	    // Funktioniert nicht, weil der Eintrag durch die extension-list Ã¼berschrieben wird.
	    // @see: AbstractInProcessServerBuilder
	    //.withConfig(ServerSettings.third_party_packages.name(), unmanagedExtensionsConfig)
	    .withExtension(unmanagedExtensionsPath, SankeyDataResource.class)
	    .withProcedure(Cypher.class)
	    .withFunction(Maps.class)
	    .withProcedure(Meta.class)
	    /*
	    .withFixture(FIXTURE[0])
	    .withFixture(FIXTURE[1])
	    .withFixture(FIXTURE[2])
	    .withFixture(FIXTURE[3])
	    .withFixture(FIXTURE[4])
	    .withFixture(FIXTURE[5])
	    .withFixture(FIXTURE[6])
	    .withFixture(FIXTURE[7])
	    .withFixture(FIXTURE[8])
	    .withFixture(FIXTURE[9])
	    .withFixture(FIXTURE[10])
	    .withFixture(FIXTURE[11])
	    .withFixture(FIXTURE[12])
	    .withFixture(FIXTURE[13])
	    .withFixture(FIXTURE[14])
	    */
	    ;
	  @Before
	  public void createSummaryNode() {
	    logMessage("Create Summary Node", null, false);
	    Map<String, Object> parameters = new HashMap<String, Object>();
	    Map<String, Object> properties = new HashMap<String, Object>();
	    properties.put("listOfCounts", listOfCounts);
	    properties.put("listOfLabels", listOfLabels);
	    parameters.put("properties", properties);
	    Result result = neo4j.getGraphDatabaseService().execute(CYPHER_SUMMARY_CREATE_WITH_PARAMS, parameters);
	    System.out.println(result.toString() + "\n" + result.resultAsString());
	  }
	  @After
	  public void backupLogMessages() {
	  File serverFolder = new File(serverPath);
	  Arrays.stream(serverFolder.listFiles())
	    .filter(f -> f.isDirectory())
	      .forEach(d -> 
	      {
	          Optional<File> logFile = 
	            Arrays.stream(d.listFiles())
	              .filter(fnd -> !fnd.isDirectory())
	              .filter(ff -> ff.getName().indexOf("neo4j") != -1)
	              .findFirst();
	          logFile.ifPresent(lf -> 
	          {
	            String parentFolderName 
	              = lf.getParent()
	                .substring(lf.getParent().lastIndexOf("\\")+1);
	            Path logPath 
	              = FileSystems
	                .getDefault()
	                .getPath(SankeyDataResourceTest.logsPath);
	            String absolutePath 
	              = logPath
	                .toAbsolutePath().toString();
	            try {
	              FileUtils.copyFile(
	                lf, 
	                new File(absolutePath 
	                  + "\\" + parentFolderName 
	                  + "_neo4j.log")
	              );
	            } catch (IOException ioe) {
	              ioe.printStackTrace();
	            }
	    });
	  });
	  }
	  @Test
	  public void shouldAvailableOverHttp() throws Exception {
	    // Given
	    URI serverURI = neo4j.httpURI();
	    logMessage("Server URI: " + serverURI.toString(), null, false);
	    logMessage("Extension URI: " + serverURI.resolve( unmanagedExtensionsPath ).toString(), null, false);
	   
	    logMessage("Rest URI: " + serverURI.resolve( unmanagedExtensionsPath + "/visualizationData/sankeyData/BookingSummary/min/10" ).toString(), null, false);
	    // When I access the server
	    HTTP.Response response = HTTP.GET( serverURI.toString() );
	    // Then it should reply
	    assertEquals(200, response.status());
	    // When I access the uri for the rest call
	    response = HTTP.GET( serverURI.resolve( unmanagedExtensionsPath + "/visualizationData/sankeyData/BookingSummary/min/10" ).toString() );
	    logMessage(response.rawContent(), "HTTP-Response", true);
	    // Then it should reply
	    assertEquals(200, response.status());
	  }
	  @Test
	  public void shouldSupplyAValidJsonContent() throws Exception {
	    // Given
	    URI serverURI = neo4j.httpURI();

	    logMessage("Server URI: " + serverURI.toString(), null, false);
	    logMessage("Extension URI: " + serverURI.resolve( unmanagedExtensionsPath ).toString(), null, false);
	   
	    logMessage("Rest URI: " + serverURI.resolve( unmanagedExtensionsPath + "/visualizationData/sankeyData/BookingSummary/max/105" ).toString(), null, false);
	    
	    // When I access the uri for the rest call
	    HTTP.Response response = HTTP.GET( serverURI.resolve( unmanagedExtensionsPath + "/visualizationData/sankeyData/BookingSummary/max/105" ).toString() );
	    logMessage(String.valueOf(response.status()),"HTTP-Status", true);
	    logMessage(response.rawContent(), "HTTP-Response", true);
	  }
	  /*
	  private static final String[] FIXTURE = {
	    "FOREACH (r IN range(0,405) | CREATE (:OBJEKT:EL2 {id:r}));",
	    "FOREACH (r IN range(0,379) | CREATE (:OBJEKT:EL3 {id:r}));",
	    "FOREACH (r IN range(0,57) | CREATE (:OBJEKT:EL13 {id:r}));",
	    "FOREACH (r IN range(0,79) | CREATE (:OBJEKT:EL6 {id:r}));",
	    "FOREACH (r IN range(0,792) | CREATE (:OBJEKT:EL0 {id:r}));",
	    "FOREACH (r IN range(0,123) | CREATE (:OBJEKT:EL14 {id:r}));",
	    "FOREACH (r IN range(0,347) | CREATE (:OBJEKT:EL4 {id:r}));",
	    "FOREACH (r IN range(0,319) | CREATE (:OBJEKT:EL5 {id:r}));",
	    "FOREACH (r IN range(0,29) | CREATE (:OBJEKT:EL8 {id:r}));",
	    "FOREACH (r IN range(0,128) | CREATE (:OBJEKT:EL12 {id:r}));",
	    "FOREACH (r IN range(0,86) | CREATE (:OBJEKT:EL9 {id:r}));",
	    "FOREACH (r IN range(0,33) | CREATE (:OBJEKT:EL8 {id:r}));",
	    "FOREACH (r IN range(0,39) | CREATE (:OBJEKT:EL7 {id:r}));",
	    "FOREACH (r IN range(0,19) | CREATE (:OBJEKT:EL15 {id:r}));",
	    "FOREACH (r IN range(0,33) | CREATE (:OBJEKT:EL8 {id:r}));"
	  };
	  */
	 
	  private static final String CYPHER_SUMMARY_CREATE_WITH_PARAMS = "CREATE (n:BookingSummary $properties) return n; ";
	  
	  private void logMessage(String msg, String title, boolean frame) {
	    if(!frame) {
	      System.out.println(testLogPrefix + ": " + msg);
	    } else {
	      System.out.println(testLogPrefix + "\n" + testLogFrame);
	      System.out.println(testLogFrameTitlePart + " " + title + " " + testLogFrameTitlePart);
	      System.out.println(msg);
	      System.out.println(testLogFrame);
	    }
	  }
	}
