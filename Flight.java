import java.time.*;

public class Flight {

    private int flightID;
    private DayTime depTime;
    private Location source;
    private Location desLocation;
    private int capacity;
    private double price;
    private int booked;

    public Flight(int flightID, DayTime depTime, Location source, Location desLocation, int capacity,
            int booked) {
        this.flightID = flightID;
        this.depTime = depTime;
        this.source = source;
        this.desLocation = desLocation;
        this.capacity = capacity;
        this.booked = booked;
    }

    public Flight(Flight f) {
        this.flightID = f.getFlightID();
        this.depTime = f.getDepTime();
        this.source = f.getSource();
        this.desLocation = f.getDesLocation();
        this.capacity = f.getCapacity();
        this.booked = f.getBooked();
    }

    // get number of minutes this flight takes
    public int getDuration() {
        final double speed = 720; // average speed of the aircraft km/h
        double duration = ((double) this.getDistance() / speed);

        return (int) Math.round(duration * 60);
    }

    // implement the ticket price formula
    public double getTicketPrice() {
        double multiplier = 0;
        double propotionFilled = ((double) this.getBooked() / capacity);

        if (propotionFilled >= 0 && propotionFilled <= 0.5)
            multiplier = (-0.4 * propotionFilled) + 1;
        else if (propotionFilled > 0.5 && propotionFilled <= 0.7)
            multiplier = propotionFilled + 0.3;
        else if (propotionFilled > 0.7 && propotionFilled <= 1)
            multiplier = (0.2 / Math.PI) * Math.atan(20 * propotionFilled - 14) + 1;

        return multiplier * (this.getDistance() / 100)
                * (30 + 4 * (this.getDesLocation().getDemand() - this.getSource().getDemand()));
    }

    // book the given number of passengers onto this flight
    // returning total cost
    public double book(int num) {

        if(num <= 0) throw new IllegalArgumentException("Invalid number of passengers to book.");

        int realNum = Math.min(this.getCapacity() - this.getBooked(), num);
        double val = 0;
        for(int i = 0; i < realNum; i++) {
            val += getTicketPrice();
            this.setBooked(this.getBooked() + 1);
        }
        val = Math.round(val * 100.0) / 100.0;

        if(realNum > 0) {
            System.out.printf("Booked %d passengers on flight %d for a total cost of $%.2f%n", realNum,
                    this.getFlightID(), val);
        }

        if(this.getBooked() == this.getCapacity()) {
            System.out.println("Flight is now full.");
        }

        return val;
    }

    // return whether this flight is full
    public boolean isFull() {
        if (this.getBooked() >= this.getCapacity())
            return true;
        return false;
    }

    // get distance of flight in km
    public double getDistance() {
        return Math.round(Location.distance(this.getSource(), this.getDesLocation()) * 10000.0) / 10000.0;
    }

    // get the layover time, in minutes, between two flights
    public static int layover(Flight x, Flight y) {
        // get the difference between the two flights in seconds
        int layover = y.getDepTime().getFlightTime().toSecondOfDay() - x.getArrivalTime().getFlightTime().toSecondOfDay();
        if (layover < 0) // check which flight comes first
            layover = x.getDepTime().getFlightTime().toSecondOfDay() - y.getArrivalTime().getFlightTime().toSecondOfDay();

        return layover / 60;
    }

    // get the arrival time
    public DayTime getArrivalTime() {
        LocalTime timeFlight = depTime.getFlightTime().plusMinutes(this.getDuration());
        DayTime.weekDay dayOfWeek = depTime.getDayOfWeek();
        int secondsPassed = depTime.getFlightTime().toSecondOfDay() + this.getDuration() * 60;
        int increment = 0;

        // check how many day will pass when flight fly
        if (secondsPassed > (24 * 60 * 60 - 1)){
            increment = secondsPassed / (24 * 60 * 60 - 1);
        }

        for (int i = 0; i < increment; i++){
            dayOfWeek = dayOfWeek.next();
        }
        StringBuilder temp = new StringBuilder();
        temp.append(dayOfWeek.toString().toUpperCase());
        temp.append(" ");
        temp.append(timeFlight.toString());
        DayTime arrivalTime = new DayTime(temp.toString());

        return arrivalTime; 
    }

    public String toString() {
        StringBuilder temp = new StringBuilder();
        temp.append("   ");
        temp.append(this.getFlightID());
        temp.append(" ");
        temp.append(this.getDepTime().getDayOfWeek());
        temp.append(" ");
        temp.append(this.getDepTime().getFlightTime());
        temp.append("   ");
        temp.append(this.getArrivalTime().getDayOfWeek());
        temp.append("   ");
        temp.append(this.getArrivalTime().getFlightTime());
        temp.append(" ");
        temp.append(this.getSource().getName());
        temp.append(" --> ");
        temp.append(this.getDesLocation().getName());
        temp.append("\n");

        return temp.toString();
    }

    // GETTER SETTER methods
    public int getFlightID() {
        return this.flightID;
    }

    public void setFlightID(int flightID) {
        this.flightID = flightID;
    }

    public DayTime getDepTime() {
        return this.depTime;
    }

    public void setDepTime(DayTime depTime) {
        this.depTime = depTime;
    }

    public Location getSource() {
        return this.source;
    }

    public void setSource(Location source) {
        this.source = source;
    }

    public Location getDesLocation() {
        return this.desLocation;
    }

    public void setDesLocation(Location desLocation) {
        this.desLocation = desLocation;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    public int getBooked() {
        return this.booked;
    }

    public void setBooked(int booked) {
        this.booked = booked;
    }
}