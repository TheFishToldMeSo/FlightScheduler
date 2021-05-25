import java.util.ArrayList;

public class Location {

  private String name;
  private double lat;
  private double lon;
  private double demand;
  private ArrayList<Flight> departingFlights;
  private ArrayList<Flight> arrivingFlights;

  public Location(String name, double lat, double lon, double demand) {
    this.name = name;
    this.lat = lat;
    this.lon = lon;
    this.demand = demand;
    this.departingFlights = new ArrayList<>();
    this.arrivingFlights = new ArrayList<>();
  }

  public Location(Location l) {
    this.name = l.getName();
    this.lat = l.getLat();
    this.lon = l.getLon();
    this.demand = l.getDemand();
  }

  // implement the Haversine formular - return value in kilometres
  public static double distance(Location l1, Location l2) {
    double lat1 = l1.getLat();
    double lat2 = l2.getLat();
    double lon1 = l1.getLon();
    double lon2 = l2.getLon();

    // distance between latitudes and longitudes
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    // convert to radians
    lat1 = Math.toRadians(lat1);
    lat2 = Math.toRadians(lat2);

    // apply formulae
    double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
    double rad = 6371;
    double c = 2 * Math.asin(Math.sqrt(a));
    return rad * c;
  }

  /**
   * Check if Fight f can arrive at this location conflict determined by if any
   * other flights are arriving/departing at this location within an hour of this
   * flight's arrival time
   * 
   * @param f the flight to check
   * @return "Flight <id> [departing/arriving] from <name> on
   *         <clashingFlightTime>". Return null if there is no clash
   */
  public String hasRunwayArrivalSpace(Flight f) {
    if (this.getDepartingFlights().size() > 0) {
      for (Flight tempFlight : this.getDepartingFlights()) {
        // checking if any of the existing flights' departing time clashes with this
        // arriving time
        if (f.getArrivalTime().getFlightTime().compareTo(tempFlight.getDepTime().getFlightTime().plusHours(1)) <= 0 || f
            .getArrivalTime().getFlightTime().compareTo(tempFlight.getDepTime().getFlightTime().minusHours(1)) > 0) {
          StringBuilder tempString = new StringBuilder();
          tempString.append("Flight ");
          tempString.append(tempFlight.getFlightID());
          tempString.append(" departing from ");
          tempString.append(this.getName());
          tempString.append(" on ");
          tempString.append(tempFlight.getDepTime().getFlightTime());

          return tempString.toString();
        }

        // checking if any of the existing flights' arriving time clashes with this
        // arriving time
        if (f.getArrivalTime().getFlightTime().compareTo(tempFlight.getArrivalTime().getFlightTime().plusHours(1)) <= 0
            || f.getArrivalTime().getFlightTime()
                .compareTo(tempFlight.getArrivalTime().getFlightTime().minusHours(1)) > 0) {
          StringBuilder tempString = new StringBuilder();
          tempString.append("Flight ");
          tempString.append(tempFlight.getFlightID());
          tempString.append(" arriving from ");
          tempString.append(this.getName());
          tempString.append(" on ");
          tempString.append(tempFlight.getArrivalTime().getFlightTime());

          return tempString.toString();
        }
      }
    }
    return null;
  }

  // GETTER SETTER methods
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getLat() {
    return this.lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return this.lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public double getDemand() {
    return this.demand;
  }

  public void setDemand(double demand) {
    this.demand = demand;
  }

  public ArrayList<Flight> getArrivingFlights() {
    return this.arrivingFlights;
  }

  public void setArrivingFlights(ArrayList<Flight> arrivingFlights) {
    this.arrivingFlights = arrivingFlights;
  }

  public ArrayList<Flight> getDepartingFlights() {
    return this.departingFlights;
  }

  public void setDepartingFlights(ArrayList<Flight> departingFlights) {
    this.departingFlights = departingFlights;
  }

}