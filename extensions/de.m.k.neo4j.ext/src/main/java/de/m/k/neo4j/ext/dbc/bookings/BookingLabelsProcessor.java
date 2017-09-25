package de.m.k.neo4j.ext.dbc.bookings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;
/**
 * BookingLabelsProcessor
 */
public class BookingLabelsProcessor {
  // This gives us a log instance that outputs messages to the
  // standard log, `neo4j.log`
  @Context
  public Log log;
  private String processBookingInformations(
    @Name("labels") String labels, 
    @Name(value = "delimiter", defaultValue = ",") String delimiter){
    String convertedPath = null;
    String splitRegex = null;
    if(delimiter.equals("|")) {
      splitRegex = "\\" + delimiter;
    }
    String[] parts = labels.split(splitRegex);
    Stream<String> partsStream = Arrays.stream(parts);
    
    List<BookingLabel> pathElementsList = 
      partsStream
        .filter(p -> 
          { 
            try {
              BookingLabel.valueOf(p);
              return true;
            } catch (Exception e) {
              log.info("Could not identify label name, label will be filtered out. Exception message: ", e);
              return false;
            }
          }
        )
        .map( p -> BookingLabel.valueOf(p))
        .sorted((p1, p2) -> Long.compare(p1.getWeight(), p2.getWeight()))
        .collect(Collectors.toList());
    
    Supplier<Stream<BookingLabel>> streamSupplier = () -> pathElementsList.stream();
    Map<Long, Long> elementGroups = streamSupplier
      .get()
      .collect( Collectors.groupingBy( b -> Long.valueOf( b.getWeight() ),
                                      Collectors.counting() ) );
    elementGroups
      .forEach(
          (key, value) -> 
            {
              if(value > 1) {
                throw new RuntimeException("Elements with the same weight are not supported!");
              }
            }
      );
    
    List<String> rawPathElementsList = streamSupplier
      .get().map(e -> e.name()).collect(Collectors.toList());
    
    convertedPath = String.join(delimiter, rawPathElementsList);
    return convertedPath;
  }
  @UserFunction
  @Description(
    "de.m.k.neo4j.ext.dbc.bookings.processLabelsOfBooking('<Delimited_LabelParts>', delimiter)" + 
    "- bring the labels of a booking in correct order."
    )
  public String processLabelsOfBooking(
    @Name("labels") String labels, 
    @Name(value = "delimiter", defaultValue = ",") String delimiter) {
    return processBookingInformations(labels, delimiter);
  }
  @UserFunction
  @Description(
    "de.m.k.neo4j.ext.dbc.bookings.processLabelsOfBookingList(['<Delimited_LabelParts>','<Delimited_LabelParts>',...], delimiter)" + 
    "- bring the labels of more then one booking in correct order."
    )
  public List<String> processLabelsOfBookingList(
    @Name("labelsList") List<String> labelsList, 
    @Name(value = "delimiter", defaultValue = ",") String delimiter) {
    List<String> convertedList = labelsList.stream()
      .map(l -> processBookingInformations(l, delimiter))
      .collect(Collectors.toList());
    return convertedList;
  }
}
