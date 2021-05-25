import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FlightHandler {
    public static boolean argsHandler(FlightScheduler instance, String[] args) {

        if (args.length >= 2) {
            args[1] = args[1].toUpperCase();
        } else {
            System.out.println(
                    "Usage:\nFLIGHT <id> [BOOK/REMOVE/RESET] [num]\nFLIGHT ADD <departure time> <from> <to> <capacity>\nFLIGHT IMPORT/EXPORT <filename>");
            return true;
        }

        switch (args[1]) {
        case "ADD":
            if (args.length < 7) {
                System.out.println("Usage:   FLIGHT ADD <departure time> <from> <to> <capacity>\n"
                        + "Example: FLIGHT ADD Monday 18:00 Sydney Melbourne 120");
            } else {
                try {
                    instance.addFlight(args[2].toUpperCase().concat(" ").concat(args[3]), args[4], args[5], args[6]);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return true;
                }
            }
            return true;

        case "IMPORT":
            try {
                instance.importFlights(args[2]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return true;

        case "EXPORT":
            try {
                instance.exportFlights(args[2]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return true;

        default: {
            int id;
            try {
                id = Integer.parseInt(args[1]);
                boolean flag = false;
                for (Flight flight : instance.getFlightList()) {
                    if (flight.getFlightID() == id) {
                        flag = true;
                    }
                }
                if (!flag)
                    throw new IllegalArgumentException();
            } catch (Exception e) {
                System.out.println("Invalid Flight ID.");
                return true;
            }

            String extraArg = (args.length >= 3) ? args[2].toUpperCase() : "";

            Flight chosenFlight = null;
            for (Flight flight : instance.getFlightList()) {
                if (flight.getFlightID() == id) {
                    chosenFlight = flight;
                }
            }

            switch (extraArg) {
                case "BOOK":
                    int passengers;
                    try {
                        try {
                            if (args.length <= 3) {
                                passengers = 1;
                            } else {
                                passengers = Integer.parseInt(args[3]);
                            }

                        } catch (Exception e) {
                            throw new IllegalArgumentException("Invalid number of passengers to book.");
                        }
                        chosenFlight.book(passengers);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return true;
                    }

                    return true;

                case "REMOVE":
                    //first remove the flight from its source and destination's flight lists
                    ArrayList<Flight> flightAtSource = chosenFlight.getSource().getDepartingFlights();
                    flightAtSource.remove(chosenFlight);

                    ArrayList<Flight> flightAtDes = chosenFlight.getDesLocation().getDepartingFlights();
                    flightAtDes.remove(chosenFlight);

                    //then remove the flight from the list of flights available
                    instance.getFlightList().remove(chosenFlight);
                    System.out.printf("Removed Flight %d, %s %s --> %s, from the flight schedule.%n",
                            chosenFlight.getFlightID(), chosenFlight.getDepTime().toString(),
                            chosenFlight.getSource().getName(), chosenFlight.getDesLocation().getName());
                    return true;

                case "RESET":
                    chosenFlight.setBooked(0);
                    System.out.printf("Reset passengers booked to 0 for Flight %d, %s %s --> %s.%n",
                            chosenFlight.getFlightID(), chosenFlight.getDepTime().toString(),
                            chosenFlight.getSource().getName(), chosenFlight.getDesLocation().getName());
                    return true;

                default:
                    Flight temp = chosenFlight;

                    System.out.printf("Flight %d%n", temp.getFlightID());
                    System.out.printf("Departure:    %s %s%n", temp.getDepTime().toString(), temp.getSource().getName());
                    System.out.printf("Arrival:      %s %s%n", temp.getArrivalTime().toString(),
                            temp.getDesLocation().getName());
                    System.out.printf("Distance:     %,dkm%n", Math.round(temp.getDistance()));
                    System.out.printf("Duration:     %sh %sm%n", temp.getDuration() / 60, temp.getDuration() % 60);
                    System.out.printf("Ticket Cost:  $%.2f%n", Math.round(temp.getTicketPrice() * 100.0) / 100.0);
                    System.out.printf("Passengers:   %d/%d%n", temp.getBooked(), temp.getCapacity());
                    return true;
                }

            }
        }
    }

    public static boolean flightsHandler(FlightScheduler instance, String[] args) {
        StringBuilder temp = new StringBuilder();
        temp.append("Flights\n");
        temp.append("-------------------------------------------------------\n");
        temp.append("ID   Departure   Arrival     Source --> Destination\n");
        temp.append("-------------------------------------------------------");
        System.out.println(temp);

        List<Flight> list = instance.getFlightList().stream().sorted(Comparator.comparing(Flight::getDepTime)
                                                            .thenComparing(f -> f.getSource().getName())).collect(Collectors.toList());

        for (Flight flight : list) {
            System.out.printf("%4s %s   %s   %s --> %s%n", flight.getFlightID(), flight.getDepTime().toString(),
                    flight.getArrivalTime().toString(), flight.getSource().getName(),
                    flight.getDesLocation().getName());

        }
        if (instance.getFlightList().isEmpty()) {
            System.out.println("(None)");
        }

        return true;
    }

}