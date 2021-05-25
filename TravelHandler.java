import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class TravelHandler {

    private static class TravelSchedule {
        private final List<Flight> flightList;

        public TravelSchedule(List<Flight> flightList) {

            this.flightList = new ArrayList<>(flightList);
        }

        public List<Flight> getFlightList() {
            return this.flightList;
        }

        public long duration() {
            AtomicLong ans = new AtomicLong();
            IntStream.of(0, flightList.size() - 1)
                    .forEach(i -> {
                        if(i > 0) {
                            int val =
                                    flightList.get(i).getDepTime().getSumMinute()
                                            - flightList.get(i - 1).getArrivalTime().getSumMinute();
                            if(val < 0) val += 24 * 6 * 60 + 23 * 60 + 59 + 1;
                            ans.addAndGet(val);
                        }
                        ans.addAndGet(flightList.get(i).getDuration());
                    });
            return ans.get();
        }

        public double cost() {
            return this.getFlightList().stream().mapToDouble(Flight::getTicketPrice).sum();
        }

        public int stopovers() {
            return this.getFlightList().size() - 1;
        }

        public long flightTime() {
            return this.getFlightList().stream().mapToLong(Flight::getDuration).sum();
        }

        public long layoverTime() {
            return this.duration() - this.flightTime();
        }
    }

    private static void travel(List<TravelSchedule> travelList, List<Flight> flights, Location currentLocation,
                        Location target) {
        if(currentLocation.getName().equalsIgnoreCase(target.getName())) {
            travelList.add(new TravelSchedule(flights));
            return ;
        }
        if(flights.size() > 3)
            return ;

        currentLocation.getDepartingFlights()
                .forEach((f) -> {
                    flights.add(f);
                    travel(travelList, flights, f.getDesLocation(), target);
                    flights.remove(flights.size() - 1);
                });
    }

    private static List<TravelSchedule> generateSchedule(Location source, Location target) {
        List<Flight> flights = new ArrayList<>();
        List<TravelSchedule> travelList = new ArrayList<>();
        travel(travelList, flights, source, target);
        return travelList;
    }

    private static void printTravelSchedule(TravelSchedule travelSchedule) {
        System.out.printf("Legs:             %d%n", travelSchedule.getFlightList().size());
        System.out.printf("Total Duration:   %dh %dm%n", travelSchedule.duration() / 60,
                travelSchedule.duration() % 60);
        System.out.printf("Total Cost:       $%.2f%n", Math.round(travelSchedule.cost() * 100.0) / 100.0);
        System.out.println(
                "-------------------------------------------------------------\n" +
                "ID   Cost      Departure   Arrival     Source --> Destination\n" +
                "-------------------------------------------------------------");
        IntStream.range(0, travelSchedule.getFlightList().size())
        .forEach(i -> {
            Flight currentFlight = travelSchedule.getFlightList().get(i);
            if(i > 0) {
                Flight prevFlight = travelSchedule.getFlightList().get(i - 1);
                long layoverDuration =
                        currentFlight.getDepTime().getSumMinute() - prevFlight.getArrivalTime().getSumMinute();
                System.out.printf("LAYOVER %dh %dm at %s%n", layoverDuration / 60,
                        layoverDuration % 60, currentFlight.getSource().getName());
            }
            System.out.printf("%4s $%8s %s   %s   %s --> %s%n",
                    currentFlight.getFlightID(),
                    Math.round(currentFlight.getTicketPrice() * 100.0) / 100.0,
                    currentFlight.getDepTime().toString(),
                    currentFlight.getArrivalTime().toString(),
                    currentFlight.getSource().getName(),
                    currentFlight.getDesLocation().getName());

        });
    }

    public static boolean argsHandler(FlightScheduler instance, String[] args) {
        Location source = null;
        Location destination = null;

        if (args.length < 3) {
            System.out.println("Usage: TRAVEL <from> <to> [cost/duration/stopovers/layover/flight_time]");
            return true;
        }

        for (Location loca : instance.getLocationList()) {
            if (loca.getName().equalsIgnoreCase(args[1])) {
                source = loca;
                break;
            }

        }

        for (Location loca : instance.getLocationList()) {
            if (loca.getName().equalsIgnoreCase(args[2])) {
                destination = loca;
                break;
            }

        }

        if (source == null) {
            System.out.println("Starting location not found.");
            return true;
        }

        if (destination == null) {
            System.out.println("Ending location not found.");
            return true;
        }

        String scheme = (args.length < 4) ? "duration" : args[3];

        List<TravelSchedule> travels = generateSchedule(source, destination);

        switch (scheme) {
            case "cost" : {
                travels.sort(Comparator.comparingDouble(TravelSchedule::cost)
                        .thenComparingLong(TravelSchedule::duration));
                break;
            }
            case "duration" : {
                travels.sort(Comparator.comparingLong(TravelSchedule::duration)
                        .thenComparingDouble(TravelSchedule::cost));
                break;
            }
            case "stopovers" : {
                travels.sort(Comparator.comparingInt(TravelSchedule::stopovers)
                        .thenComparingLong(TravelSchedule::duration)
                        .thenComparingDouble(TravelSchedule::cost));
                break;
            }
            case "layover" : {
                travels.sort(Comparator.comparingLong(TravelSchedule::layoverTime)
                        .thenComparingLong(TravelSchedule::duration)
                        .thenComparingDouble(TravelSchedule::cost));
                break;
            }
            case "flight_time" : {
                travels.sort(Comparator.comparingLong(TravelSchedule::flightTime)
                        .thenComparingLong(TravelSchedule::duration)
                        .thenComparingDouble(TravelSchedule::cost));
                break;
            }
            default : {
                System.out.println("Invalid sorting property: must be either cost, duration, stopovers, layover, or flight_time.");
                return true;
            }
        }

        int index = 1;

        try {
            index = (args.length < 5) ? 1 : Integer.parseInt(args[4]);
        }
        catch(Exception e) {
            // pass
        }

        index = Math.min(index, travels.size());
        index = Math.max(index, 1);
        if(travels.isEmpty()) {
            System.out.printf("Sorry, no flights with 3 or less stopovers are " +
                    "available from %s to %s.%n", source.getName(), destination.getName());
        }
        else {
            TravelSchedule travelSchedule = travels.get(index - 1);
            printTravelSchedule(travelSchedule);
        }
        return true;
    }
}
