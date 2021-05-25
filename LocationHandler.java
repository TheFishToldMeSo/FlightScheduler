import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

public class LocationHandler {

    public static boolean argsHandler(FlightScheduler instance, String[] args) {
        if (args.length <= 1) {
            System.out.println("Usage:\nLOCATION <name>\nLOCATION ADD" + " <name> <latitude> <longitude>"
                    + " <demand_coefficient>\nLOCATION" + " IMPORT/EXPORT <filename>");
            return true;
        } else {
            args[1] = args[1].toUpperCase();
        }

        switch (args[1]) {
        case "ADD":
            if (args.length < 6) {
                System.out.println("Usage:   LOCATION ADD <name> <lat> <long> <demand_coefficient>\n"
                        + "Example: LOCATION ADD Sydney -33.847927 150.651786 0.2");
                return true;
            } else {
                try {
                    instance.addLocation(args[2], args[3], args[4], args[5], false);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return true;
                }
            }
            return true;

        case "IMPORT":
            instance.importLocations(args);
            return true;

        case "EXPORT":
            try {
                instance.exportLocations(args[2]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return true;
            }
            return true;

        default:
            try {
                for (Location loca : instance.getLocationList()) {
                    if (loca.getName().equalsIgnoreCase(args[1])) {
                        System.out.println("Location:    " + loca.getName());
                        System.out.printf("Latitude:    %6f%n", loca.getLat());
                        System.out.printf("Longitude:   %6f%n", loca.getLon());
                        System.out.printf("Demand:      %+.4f%n", loca.getDemand());
                        return true;
                    }
                }
                throw new IllegalArgumentException("Invalid location name.");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return true;
            }
        }
    }

    public static boolean locationsHandler(FlightScheduler instance, String[] args) {
        try {
            class LocationInfo {
                private final String name;

                public LocationInfo(Location loca) {
                    this.name = loca.getName();
                }

                public String getName() {
                    return name;
                }
            }

            // print out the "Location(n) ":
            int size = instance.getLocationList().size();
            System.out.printf("Locations (%d):%n", instance.getLocationList().size());

            if (size < 1)
                throw new IllegalArgumentException("(None)");

            // sort and print out the rest of the locations
            StringBuilder temp = new StringBuilder();
            ArrayList<Location> locationList = instance.getLocationList();
            Stream<LocationInfo> fullLocations;

            fullLocations = locationList.stream().map(loca -> new LocationInfo(loca));

            if (fullLocations != null)
                fullLocations.sorted(Comparator.comparing(LocationInfo::getName)).forEach(fi -> {
                    temp.append(fi.getName());
                    temp.append(", ");
                });
            System.out.printf(temp.toString().substring(0, temp.length() - 2));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return true;
        }

        return true;
    }
}