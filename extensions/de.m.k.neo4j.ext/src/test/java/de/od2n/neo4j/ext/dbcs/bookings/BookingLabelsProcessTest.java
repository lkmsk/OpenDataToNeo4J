package de.od2n.neo4j.ext.dbcs.bookings;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.harness.junit.Neo4jRule;

import de.od2n.neo4j.ext.dbcs.bookings.BookingLabelsProcessor;
public class BookingLabelsProcessTest {
  // This rule starts a Neo4j instance
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            // This is the function we want to test
            .withFunction( BookingLabelsProcessor.class );
    
    private String runProcedureCall(String procedureCall, String resultVariable) throws Throwable {
      // This is in a try-block, to make sure we close the driver after the test
      try( Driver driver = GraphDatabase
              .driver( neo4j.boltURI() , Config.build().withEncryptionLevel( Config.EncryptionLevel.NONE ).toConfig() ) )
      {
          // Given
          Session session = driver.session();
          // When
          String result = session.run( procedureCall ).single().get(resultVariable).asString();
          return result;
      }
    }
    private List<String> runProcedureCallSuppliesList(String procedureCall, String resultVariable) throws Throwable {
      // This is in a try-block, to make sure we close the driver after the test
      try( Driver driver = GraphDatabase
              .driver( neo4j.boltURI() , Config.build().withEncryptionLevel( Config.EncryptionLevel.NONE ).toConfig() ) )
      {
          // Given
          Session session = driver.session();
          // When
          StatementResult result = session.run( procedureCall );
/*
          while ( result.hasNext() )
          {
            Record record = result.next();
            System.out.println( record.get( "result" ).asList().size() );
          }
*/
          List<String> list = result.single().get(resultVariable).asList(m -> m.asString());
          for (String var : list) {
            System.out.println("List part: " + var);
          }
          return list;
      }
    }
    @Test
    public void shouldSortBookingLabelsCorrectly() throws Throwable {
      String result = runProcedureCall( "RETURN od2n.dbcs.processLabelsOfBooking('BOOKING|APP|KLEINKLASSE|SHORT_DISTANCE|MULTICITY','|') AS result", "result");
      assertThat( result, equalTo( "BOOKING|KLEINKLASSE|APP|MULTICITY|SHORT_DISTANCE" ) );
    }
    @Test(expected = RuntimeException.class)
    public void shouldNotSupportDuplicateWeightValues() throws Throwable {
      runProcedureCall( "RETURN od2n.dbcs.processLabelsOfBooking('BOOKING|APP|KLEINKLASSE|SHORT_DISTANCE|MULTICITY|LONG_DISTANCE','|') AS result", "result");
    }
    @Test
    public void shouldFilterNotNeededBookingLabelParts() throws Throwable {
      String result = runProcedureCall( "RETURN od2n.dbcs.processLabelsOfBooking('BOOKING|APP|KLEINKLASSE|SHORT_DISTANCE|MULTICITY|OPEL','|') AS result", "result");
      assertThat( result, not(containsString(( "XYZ" ))) );
    }
    @Test
    public void shouldHandleListOfLabelsCorrectly() throws Throwable {
      String path1 = "'BOOKING|APP|KLEINKLASSE|SHORT_DISTANCE|MULTICITY'";
      String path2 = "'BOOKING|APP|KLEINKLASSE|SHORT_DISTANCE|LONG_DISTANCE|MULTICITY'";
      String path3 = "'BOOKING|LONG_DISTANCE|INTERNET|DB|MITTELKLASSE'";
      String listOfPaths1 = "[" + path1 + "," + path3 + "]";
      String listOfPaths2 = "[" + path1 + "," + path2 + "]";
      List<String> result = runProcedureCallSuppliesList( "RETURN od2n.dbcs.processLabelsOfBookingList(" + listOfPaths1 + ",'|') AS result", "result");
      assertThat( result.get(0), not(containsString(( "NOT_SUPPORTED" ))) );
      assertThat( result.get(1), not(containsString(( "NOT_SUPPORTED" ))) );
      assertThat( result.get(0), equalTo( "BOOKING|KLEINKLASSE|APP|MULTICITY|SHORT_DISTANCE" ) );
      assertThat( result.get(1), equalTo( "BOOKING|MITTELKLASSE|INTERNET|DB|LONG_DISTANCE" ) );
      try {
        runProcedureCallSuppliesList( "RETURN od2n.dbcs.processLabelsOfBookingList(" + listOfPaths2 + ",'|') AS result", "result");
        fail();
      } catch (Exception e) {
        assertTrue(true);
      }
    }
}
