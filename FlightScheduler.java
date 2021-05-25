import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class FlightScheduler {
  // class instance to work on
  private static FlightScheduler instance;
  private String[] args;
  private ArrayList<Flight> flightList = new ArrayList<>();
  private ArrayList<Location> locationList = new ArrayList<>();
  private static Scanner scanner;

  private int lastFlightID = -1;

  // constructor that takes in arguments from terminal
  // handle commands user puts in
  public FlightScheduler(String[] args) {
    this.args = args;
  }

  public int addFlight(String date1, String start, String end, String capacity) {
    return addFlight(date1, start, end, capacity, 0, false);

  }

  // Add a flight to the database
  // handle error cases and return status negative if error
  // (different status codes for different messages)
  // do not print out anything in this function
  // return 1 if can't add flight
  public int addFlight(String departTime, String start, String end, String capacity, int booked, boolean isImported) {

    DayTime time = null;
    int capacityNum;
    int status = 0;

    Location source = null;

    for (Location loca : locationList) {
      if (loca.getName().equalsIgnoreCase(start))
        source = loca;
    }

    Location destination = null;
    for (Location loca : locationList) {
      if (loca.getName().equalsIgnoreCase(end))
        destination = loca;
    }

    if (source == null) {
      status = 1;
      throw new IllegalArgumentException("Invalid starting location.");
    }

    if (destination == null) {
      status = 1;
      throw new IllegalArgumentException("Invalid ending location.");
    }

    try {
      time = new DayTime(departTime);
    } catch (Exception e) {
      status = 1;
      throw e;
    }

    try {
      capacityNum = Integer.parseInt(capacity);
      if (capacityNum <= 0)
        throw new IllegalArgumentException();
    } catch (Exception e) {
      status = 1;
      throw new IllegalArgumentException("Invalid positive integer capacity.");
    }

    if (source == destination) {
      status = 1;
      throw new IllegalArgumentException("Source and destination cannot be the same place.");
    }

    // TODO: handling conflicts in here

    Flight newFlight = new Flight(lastFlightID + 1, time, source, destination, capacityNum, booked);
    try {
      checkConflict(newFlight, flightList);
      // // add new Flight to the list of flights
      flightList.add(newFlight);
      newFlight.getDesLocation().getArrivingFlights().add(newFlight);
      newFlight.getSource().getDepartingFlights().add(newFlight);
      lastFlightID = newFlight.getFlightID();
      if (!isImported)
        System.out.printf("Successfully added Flight %d.%n", newFlight.getFlightID());
    } catch (Exception e) {
      status = 1;
      if (!isImported)
        System.out.println(e.getMessage());
      return status;
    }
    return status;
  }

  private void checkConflict(Flight newFlight, ArrayList<Flight> flightList) {

    boolean flag = false;
    ArrayList<Flight> conflictFlights = new ArrayList<>();

    for (Flight flight : flightList) {
      if (flight.getSource().getName().equalsIgnoreCase(newFlight.getSource().getName())
          && DayTime.isConflicted(flight.getDepTime(), newFlight.getDepTime())) {
        flag = true;
        conflictFlights.add(flight);
      }
    }

    if (flag) {
      Flight conflictFlight = (conflictFlights.stream()
          .anyMatch(f -> DayTime.compare(f.getDepTime(), newFlight.getDepTime()) > 0))
              ? conflictFlights.stream().filter(f -> DayTime.compare(f.getDepTime(), newFlight.getDepTime()) > 0)
                  .min((f1, f2) -> DayTime.compare(f1.getDepTime(), f2.getDepTime())).get()
              : conflictFlights.stream().max((f1, f2) -> DayTime.compare(f1.getDepTime(), f2.getDepTime())).get();
      throw new IllegalArgumentException("Scheduling conflict! " + "This flight clashes with Flight "
          + conflictFlight.getFlightID() + " departing from " + conflictFlight.getSource().getName() + " on "
          + conflictFlight.getDepTime().toFullString() + ".");
    }

    for (Flight flight : flightList) {
      if (flight.getDesLocation().getName().equalsIgnoreCase(newFlight.getSource().getName())
          && DayTime.isConflicted(flight.getArrivalTime(), newFlight.getDepTime())) {
        flag = true;
        conflictFlights.add(flight);
      }
    }

    if (flag) {
      Flight conflictFlight = (conflictFlights.stream()
          .anyMatch(f -> DayTime.compare(f.getArrivalTime(), newFlight.getDepTime()) > 0))
              ? conflictFlights.stream().filter(f -> DayTime.compare(f.getArrivalTime(), newFlight.getDepTime()) > 0)
                  .min((f1, f2) -> DayTime.compare(f1.getArrivalTime(), f2.getArrivalTime())).get()
              : conflictFlights.stream().max((f1, f2) -> DayTime.compare(f1.getArrivalTime(), f2.getArrivalTime()))
                  .get();
      throw new IllegalArgumentException("Scheduling conflict! " + "This flight clashes with Flight "
          + conflictFlight.getFlightID() + " arriving at " + conflictFlight.getDesLocation().getName() + " on "
          + conflictFlight.getArrivalTime().toFullString() + ".");
    }

    for (Flight flight : flightList) {
      if (flight.getSource().getName().equalsIgnoreCase(newFlight.getDesLocation().getName())
          && DayTime.isConflicted(flight.getDepTime(), newFlight.getArrivalTime())) {
        flag = true;
        conflictFlights.add(flight);
      }
    }

    if (flag) {
      Flight conflictFlight = (conflictFlights.stream()
          .anyMatch(f -> DayTime.compare(f.getDepTime(), newFlight.getArrivalTime()) > 0))
              ? conflictFlights.stream().filter(f -> DayTime.compare(f.getDepTime(), newFlight.getArrivalTime()) > 0)
                  .min((f1, f2) -> DayTime.compare(f1.getDepTime(), f2.getDepTime())).get()
              : conflictFlights.stream().max((f1, f2) -> DayTime.compare(f1.getDepTime(), f2.getDepTime())).get();
      throw new IllegalArgumentException("Scheduling conflict! " + "This flight clashes with Flight "
          + conflictFlight.getFlightID() + " arriving at " + conflictFlight.getDesLocation().getName() + " on "
          + conflictFlight.getArrivalTime().toFullString() + ".");
    }

    for (Flight flight : flightList) {
      if (flight.getDesLocation().getName().equalsIgnoreCase(newFlight.getDesLocation().getName())
          && DayTime.isConflicted(flight.getArrivalTime(), newFlight.getArrivalTime())) {
        flag = true;
        conflictFlights.add(flight);
      }
    }

    if (flag) {
      Flight conflictFlight = (conflictFlights.stream()
          .anyMatch(f -> DayTime.compare(f.getArrivalTime(), newFlight.getArrivalTime()) > 0))
              ? conflictFlights.stream()
                  .filter(f -> DayTime.compare(f.getArrivalTime(), newFlight.getArrivalTime()) > 0)
                  .min((f1, f2) -> DayTime.compare(f1.getArrivalTime(), f2.getArrivalTime())).get()
              : conflictFlights.stream().max((f1, f2) -> DayTime.compare(f1.getArrivalTime(), f2.getArrivalTime()))
                  .get();
      throw new IllegalArgumentException("Scheduling conflict! " + "This flight clashes with Flight "
          + conflictFlight.getFlightID() + " departing from " + conflictFlight.getSource().getName() + " on "
          + conflictFlight.getDepTime().toFullString() + ".");
    }

    // first condition
    // if(flightList.stream().anyMatch(f => Math.abs(f.getDepTime() -
    // newFlight.getDepTime()) <= 1))
    return;
  }

  // flight import <filename>
  public void importFlights(String fileName) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
      String line;
      int count = 0;
      int err = 0;
      int status = 0;

      while ((line = br.readLine()) != null) {
        String[] flightArgs = line.split(",");
        if (flightArgs.length < 5) {
          continue;
        }
        try {
          int booked = Integer.parseInt(flightArgs[4]);
          status = addFlight(flightArgs[0], flightArgs[1], flightArgs[2], flightArgs[3], booked, true);
        } catch (Exception e) {
          err++;
          continue;
        }
        count++;
      }
      br.close();
      System.out.println("Imported " + count + " flight" + (count != 1 ? "s" : "") + ".");
      if (err > 0) {
        System.out.println(err + " line" + (err == 1 ? " was" : "s were") + " invalid.");
      }
    } catch (Exception e) {
      System.out.println("Error reading file.");
    }
  }

  public void exportFlights(String fileName) {
    try {
      FileWriter fileWriter = new FileWriter(fileName);
      PrintWriter printWriter = new PrintWriter(fileWriter);

      Stream<Flight> fullSchedule;
      fullSchedule = getFlightList().stream().map(f -> new Flight(f));

      if (fullSchedule != null)
        fullSchedule.sorted(Comparator.comparing(Flight::getFlightID))
            .forEach(fi -> printWriter.println(fi.getDepTime().toFullString() + "," + fi.getSource().getName() + ","
                + fi.getDesLocation().getName() + "," + fi.getCapacity() + "," + fi.getBooked()));

      printWriter.close();
    } catch (Exception e) {
      System.out.println("Error writing file.");
    }

  }

  // Add a location to the database
  // do not print out anything in this function
  // return 1 for error cases
  public int addLocation(String name, String lat, String lon, String demand, boolean isImported) {
    int status = 0;
    // check if there is any existing location
    for (Location location : locationList)
      if (location.getName().equalsIgnoreCase(name)) {
        status = 1;
        throw new IllegalArgumentException("This location already exists.");
      }

    double latNum;
    try {
      latNum = Double.parseDouble(lat);
      if (latNum > 85 || latNum < -85) {
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      status = 1;
      throw new IllegalArgumentException("Invalid latitude. It must be a number of degrees between -85 and +85.");
    }

    double lonNum;
    try {
      lonNum = Double.parseDouble(lon);
      if (lonNum > 180 || lonNum < -180) {
        status = 1;
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      status = 1;
      throw new IllegalArgumentException("Invalid longitude. It must be a number of degrees between -180 and +180.");
    }

    double demandNum;
    try {
      demandNum = Double.parseDouble(demand);
      if (demandNum > 1 || demandNum < -1) {
        status = 1;
        throw new IllegalArgumentException();
      }
    } catch (Exception e) {
      status = 1;
      throw new IllegalArgumentException("Invalid demand coefficient. It must be a number between -1 and +1.");
    }

    // add new Location to the list of locations
    try {
      Location newLocation = new Location(name, latNum, lonNum, demandNum);
      locationList.add(newLocation);
      if (!isImported) {
        System.out.printf("Successfully added location %s.%n", newLocation.getName());
      }
    } catch (Exception e) {
      if (!isImported)
        System.out.println(e.getMessage());
      return status;
    }
    return status;
  }

  // location import <filename>
  public void importLocations(String[] command) {
    try {
      if (command.length < 3)
        throw new FileNotFoundException();
      BufferedReader br = new BufferedReader(new FileReader(new File(command[2])));
      String line;
      int count = 0;
      int err = 0;
      int status = 0;

      while ((line = br.readLine()) != null) {
        String[] lparts = line.split(",");
        if (lparts.length < 4)
          continue;

        try {
          status = addLocation(lparts[0], lparts[1], lparts[2], lparts[3], true);
        } catch (Exception e) {
          err++;
          continue;
        }
        if (status == 1) {
          err++;
          continue;
        } else
          count++;
      }
      br.close();
      System.out.println("Imported " + count + " location" + (count != 1 ? "s" : "") + ".");
      if (err > 0) {
        if (err == 1)
          System.out.println("1 line was invalid.");
        else
          System.out.println(err + " lines were invalid.");
      }

    } catch (IOException e) {
      System.out.println("Error reading file.");
      return;
    }
  }

  public void exportLocations(String fileName) {
    try {
      FileWriter fileWriter = new FileWriter(fileName);
      PrintWriter printWriter = new PrintWriter(fileWriter);
      ArrayList<Location> locationList = instance.getLocationList();
      Stream<Location> fullLocations;

      fullLocations = locationList.stream().map(loca -> new Location(loca));

      if (fullLocations != null)
        fullLocations.sorted(Comparator.comparing(Location::getName)).forEach(loca -> {
          printWriter.println(loca.getName() + "," + loca.getLat() + "," + loca.getLon() + "," + loca.getDemand());
        });

      printWriter.close();
    } catch (Exception e) {
      System.out.println("Error writing file.");
    }
  }

  public void run() {
    boolean runCondition = true;

    while (runCondition) {
      System.out.print("User: ");
      String argStrings = scanner.nextLine();

      String[] args = argStrings.split(" ");
      try {
        runCondition = this.run_(args);

      } catch (Exception e) {

        System.out.println(e.getMessage());
      }
      System.out.print("\n");
    }
  }

  private boolean run_(String[] args) {

    if (args.length >= 1) {
      args[0] = args[0].toUpperCase();
    } else {
      System.out.println("Usage: HELP for more information.");
      return true;
    }

    switch (args[0]) {
    case "FLIGHT":
      return FlightHandler.argsHandler(this, args);
    case "FLIGHTS":
      return FlightHandler.flightsHandler(this, args);
    case "LOCATION":
      return LocationHandler.argsHandler(this, args);

    case "LOCATIONS":
      return LocationHandler.locationsHandler(this, args);

    case "TRAVEL":
      return TravelHandler.argsHandler(this, args);

    case "SCHEDULE":

    case "DEPARTURES":

    case "ARRIVALS":
      return printingFlights(args);

    case "HELP":
      return getHelp();

    case "EXIT":
      System.out.println("Application closed.");
      return false;

    default:
      System.out.println("Invalid command. Type 'help' for a list of commands.");
    }
    return true;
  }

  private boolean printingFlights(String[] args) {

    class FlightInfo {
      private final String info;
      private final DayTime time;
      private final int flightID;

      public FlightInfo(Flight flight, boolean isArriving) {
        if (isArriving) {
          info = "Arrival from " + flight.getSource().getName();
          time = flight.getArrivalTime();
        } else {
          info = "Departure to " + flight.getDesLocation().getName();
          time = flight.getDepTime();
        }

        flightID = flight.getFlightID();
      }

      public String getInfo() {
        return info;
      }

      public int getFlightID() {
        return flightID;
      }

      public DayTime getTime() {
        return time;
      }

    }

    String printingScheme = args[0];
    if (args.length < 2 || this.getLocationList().stream().noneMatch(l -> l.getName().equalsIgnoreCase(args[1]))) {
      System.out.println("This location does not exist in the system.");
    } else {
      Location location = this.getLocationList().stream().filter(l -> l.getName().equalsIgnoreCase(args[1])).findFirst()
          .get();

      Stream<FlightInfo> fullSchedule;

      switch (printingScheme) {
      case "SCHEDULE":
        fullSchedule = Stream.concat(location.getArrivingFlights().stream().map(f -> new FlightInfo(f, true)),
            location.getDepartingFlights().stream().map(f -> new FlightInfo(f, false)));
        break;
      case "DEPARTURES":
        fullSchedule = location.getDepartingFlights().stream().map(f -> new FlightInfo(f, false));
        break;
      case "ARRIVALS":
        fullSchedule = location.getArrivingFlights().stream().map(f -> new FlightInfo(f, true));
        break;
      default:
        fullSchedule = null;
      }
      System.out.println(location.getName());

      System.out.println("-------------------------------------------------------\n"
          + "ID   Time        Departure/Arrival to/from Location\n"
          + "-------------------------------------------------------");

      if (fullSchedule != null)
        fullSchedule.sorted(Comparator.comparing(FlightInfo::getTime))
            .forEach(fi -> System.out.printf("%4s %s   %s%n", fi.getFlightID(), fi.getTime(), fi.getInfo()));
      else
        System.out.println("(None)");

    }
    return true;
  }

  public static void main(String[] args) {
    scanner = new Scanner(System.in);
    instance = new FlightScheduler(args);
    instance.run();
  }

  // GETTER SETTER methods
  public static FlightScheduler getInstance() {
    return instance;
  }

  public ArrayList<Flight> getFlightList() {
    return this.flightList;
  }

  public void setFlightList(ArrayList<Flight> flightList) {
    this.flightList = flightList;
  }

  public String[] getArgs() {
    return this.args;
  }

  public void setArgs(String[] args) {
    this.args = args;
  }

  public ArrayList<Location> getLocationList() {
    return this.locationList;
  }

  public void setLocationList(ArrayList<Location> locationList) {
    this.locationList = locationList;
  }

  public int getLastFlightID() {
    return this.lastFlightID;
  }

  public void setLastFlightID(int lastFlightID) {
    this.lastFlightID = lastFlightID;
  }

  public boolean getHelp() {
    System.out.println("FLIGHTS - list all available flights ordered by departure time, then departure location name");
    System.out.println("FLIGHT ADD <departure time> <from> <to> <capacity> - add a flight");
    System.out.println("FLIGHT IMPORT/EXPORT <filename> - import/export flights to csv file");
    System.out.println(
        "FLIGHT <id> - view information about a flight (from->to, departure arrival times, current ticket price, capacity, passengers booked)");
    System.out.println(
        "FLIGHT <id> BOOK <num> - book a certain number of passengers for the flight at the current ticket price, and then adjust the ticket price to reflect the reduced capacity remaining. If no number is given, book 1 passenger. If the given number of bookings is more than the remaining capacity, only accept bookings until the capacity is full.");
    System.out.println("FLIGHT <id> REMOVE - remove a flight from the schedule");
    System.out.println(
        "FLIGHT <id> RESET - reset the number of passengers booked to 0, and the ticket price to its original state.");
    System.out.print("\n");
    System.out.println("LOCATIONS - list all available locations in alphabetical order");
    System.out.println("LOCATION ADD <name> <lat> <long> <demand_coefficient> - add a location");
    System.out.println("LOCATION <name> - view details about a location (it's name, coordinates, demand coefficient)");
    System.out.println("LOCATION IMPORT/EXPORT <filename> - import/export locations to csv file");
    System.out.println(
        "SCHEDULE <location_name> - list all departing and arriving flights, in order of the time they arrive/depart");
    System.out.println("DEPARTURES <location_name> - list all departing flights, in order of departure time");
    System.out.println("ARRIVALS <location_name> - list all arriving flights, in order of arrival time");
    System.out.print("\n");
    System.out.println(
        "TRAVEL <from> <to> [sort] [n] - list the nth possible flight route between a starting location and destination, with a maximum of 3 stopovers. Default ordering is for shortest overall duration. If n is not provided, display the first one in the order. If n is larger than the number of flights available, display the last one in the ordering.");
    System.out.print("\n");
    System.out.println("can have other orderings:");
    System.out.println("TRAVEL <from> <to> cost - minimum current cost");
    System.out.println("TRAVEL <from> <to> duration - minimum total duration");
    System.out.println("TRAVEL <from> <to> stopovers - minimum stopovers");
    System.out.println("TRAVEL <from> <to> layover - minimum layover time");
    System.out.println("TRAVEL <from> <to> flight_time - minimum flight time");
    System.out.print("\n");
    System.out.println("HELP - outputs this help string.");
    System.out.println("EXIT - end the program.");
    return true;
  }
}